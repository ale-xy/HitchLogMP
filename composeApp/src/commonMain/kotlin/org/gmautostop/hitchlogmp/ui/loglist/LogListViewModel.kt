package org.gmautostop.hitchlogmp.ui.loglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.char
import org.gmautostop.hitchlogmp.data.AuthService
import org.gmautostop.hitchlogmp.data.FirestoreSyncTracker
import org.gmautostop.hitchlogmp.domain.model.HitchLog
import org.gmautostop.hitchlogmp.domain.repository.Repository
import org.gmautostop.hitchlogmp.domain.repository.Response
import org.gmautostop.hitchlogmp.toLocalDateTime

/**
 * UI model for HitchLog with pre-formatted date string.
 */
data class HitchLogUi(
    val id: String,
    val name: String,
    val formattedDate: String  // e.g. "5.05.2026" — just the date, no "Создано" prefix
)

/**
 * UI state for LogList screen combining logs and user authentication status.
 */
data class LogListUiState(
    val logsState: org.gmautostop.hitchlogmp.ui.ViewState<List<HitchLogUi>>,
    val isAnonymousUser: Boolean,
    val hasPendingWrites: Boolean
)

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
 * Maps domain HitchLog to UI model with formatted date.
 */
fun HitchLog.toUi(): HitchLogUi {
    val date = creationTime.toLocalDateTime().date
    return HitchLogUi(
        id = id,
        name = name,
        formattedDate = dateFormat.format(date)
    )
}

class LogListViewModel(
    repository: Repository,
    authService: AuthService,
    syncTracker: FirestoreSyncTracker
): ViewModel() {
    private val _logsState = MutableStateFlow<org.gmautostop.hitchlogmp.ui.ViewState<List<HitchLogUi>>>(_root_ide_package_.org.gmautostop.hitchlogmp.ui.ViewState.Loading)

    val state: StateFlow<LogListUiState> = combine(
        _logsState,
        authService.currentUser,
        syncTracker.hasPendingWrites
    ) { logsState, user, hasPendingWrites ->
        LogListUiState(
            logsState = logsState,
            isAnonymousUser = user?.isAnonymous == true,
            hasPendingWrites = hasPendingWrites
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LogListUiState(
            logsState = _root_ide_package_.org.gmautostop.hitchlogmp.ui.ViewState.Loading,
            isAnonymousUser = false,
            hasPendingWrites = false
        )
    )

    init {
        viewModelScope.launch {
            _logsState.value = _root_ide_package_.org.gmautostop.hitchlogmp.ui.ViewState.Loading

            repository.getLogs().distinctUntilChanged()
                .collect { response ->
                    _logsState.value = when(response) {
                        is Response.Loading -> _root_ide_package_.org.gmautostop.hitchlogmp.ui.ViewState.Loading
                        is Response.Failure -> _root_ide_package_.org.gmautostop.hitchlogmp.ui.ViewState.Error(response.error)
                        is Response.Success -> _root_ide_package_.org.gmautostop.hitchlogmp.ui.ViewState.Show(response.data.map { it.toUi() })
                    }
                }
        }
    }

}