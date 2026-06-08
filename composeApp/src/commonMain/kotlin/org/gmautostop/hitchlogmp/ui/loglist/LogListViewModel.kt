package org.gmautostop.hitchlogmp.ui.loglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.char
import org.gmautostop.hitchlogmp.data.AuthService
import org.gmautostop.hitchlogmp.data.FirestoreSyncTracker
import org.gmautostop.hitchlogmp.data.SortPreferences
import org.gmautostop.hitchlogmp.domain.model.HitchLog
import org.gmautostop.hitchlogmp.domain.repository.Repository
import org.gmautostop.hitchlogmp.domain.repository.Response
import org.gmautostop.hitchlogmp.toLocalDateTime
import org.gmautostop.hitchlogmp.ui.ViewState

/**
 * UI model for HitchLog with pre-formatted date strings.
 */
data class HitchLogUi(
    val id: String,
    val name: String,
    val formattedDate: String,
    val formattedStartDate: String? = null,
    val startDate: Timestamp? = null,
    val creationTime: Timestamp? = null,
)

/**
 * Sort criteria for chronicle list.
 */
enum class SortField {
    CREATION_DATE, RECORD_DATE, NAME
}

/**
 * Sort direction.
 */
enum class SortDirection {
    DESCENDING, ASCENDING
}

data class SortConfig(
    val field: SortField = SortField.CREATION_DATE,
    val direction: SortDirection = SortDirection.DESCENDING
)

/**
 * UI state for LogList screen combining logs and user authentication status.
 */
data class LogListUiState(
    val logsState: ViewState<List<HitchLogUi>>,
    val isAnonymousUser: Boolean,
    val hasPendingWrites: Boolean,
    val sortConfig: SortConfig = SortConfig()
)

/**
 * Russian month names for date formatting.
 */
private val russianMonthNames = arrayOf(
    "янв", "фев", "мар", "апр", "мая", "июн",
    "июл", "авг", "сен", "окт", "ноя", "дек"
)

/**
 * Format date as "15 мая 2025" style.
 */
private fun LocalDate.formatRussian(): String {
    val monthName = russianMonthNames[monthNumber - 1]
    return "$dayOfMonth $monthName $year"
}

/**
 * Date format for chronicle creation date: d.MM.yyyy
 */
private val dateFormat = LocalDate.Format {
    day()
    char('.')
    monthNumber()
    char('.')
    year()
}

/**
 * Maps domain HitchLog to UI model with formatted dates.
 */
fun HitchLog.toUi(): HitchLogUi {
    val date = creationTime.toLocalDateTime().date
    val startDateFormatted = startDate?.let {
        it.toLocalDateTime().date.formatRussian()
    }
    return HitchLogUi(
        id = id,
        name = name,
        formattedDate = dateFormat.format(date),
        formattedStartDate = startDateFormatted,
        startDate = startDate,
        creationTime = creationTime,
    )
}

class LogListViewModel(
    repository: Repository,
    authService: AuthService,
    syncTracker: FirestoreSyncTracker,
    private val sortPreferences: SortPreferences
): ViewModel() {
    private val _logsState = MutableStateFlow<ViewState<List<HitchLogUi>>>(ViewState.Loading)
    private val _sortConfig = MutableStateFlow(sortPreferences.load())

    val state: StateFlow<LogListUiState> = combine(
        _logsState,
        authService.currentUser,
        syncTracker.hasPendingWrites,
        _sortConfig
    ) { logsState, user, hasPendingWrites, sortConfig ->
        val sortedState = when (logsState) {
            is ViewState.Show -> ViewState.Show(sortLogs(logsState.value, sortConfig))
            else -> logsState
        }
        LogListUiState(
            logsState = sortedState,
            isAnonymousUser = user?.isAnonymous == true,
            hasPendingWrites = hasPendingWrites,
            sortConfig = sortConfig
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LogListUiState(
            logsState = ViewState.Loading,
            isAnonymousUser = false,
            hasPendingWrites = false
        )
    )

    init {
        viewModelScope.launch {
            _logsState.value = ViewState.Loading

            repository.getLogs().distinctUntilChanged()
                .collect { response ->
                    _logsState.value = when(response) {
                        is Response.Loading -> ViewState.Loading
                        is Response.Failure -> ViewState.Error(response.error)
                        is Response.Success -> ViewState.Show(response.data.map { it.toUi() })
                    }
                }
        }
    }

    fun updateSort(sortConfig: SortConfig) {
        _sortConfig.update { sortConfig }
        sortPreferences.save(sortConfig)
    }

    private fun sortLogs(logs: List<HitchLogUi>, config: SortConfig): List<HitchLogUi> {
        return when (config.field) {
            SortField.CREATION_DATE -> {
                val sorted = logs.sortedBy { it.creationTime?.seconds ?: 0L }
                if (config.direction == SortDirection.DESCENDING) sorted.reversed() else sorted
            }
            SortField.RECORD_DATE -> {
                val (withDate, withoutDate) = logs.partition { it.startDate != null }
                val sorted = withDate.sortedBy { it.startDate!!.seconds }
                val sortedWithDate = if (config.direction == SortDirection.DESCENDING) sorted.reversed() else sorted
                val sortedWithout = withoutDate.sortedByDescending { it.creationTime?.seconds ?: 0L }
                sortedWithDate + sortedWithout
            }
            SortField.NAME -> {
                val sorted = logs.sortedBy { it.name.lowercase() }
                if (config.direction == SortDirection.DESCENDING) sorted.reversed() else sorted
            }
        }
    }
}
