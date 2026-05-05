package org.gmautostop.hitchlogmp.ui

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
import org.gmautostop.hitchlogmp.domain.HitchLog
import org.gmautostop.hitchlogmp.domain.Repository
import org.gmautostop.hitchlogmp.domain.Response
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
    val logsState: ViewState<List<HitchLogUi>>,
    val isAnonymousUser: Boolean
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
    authService: AuthService
): ViewModel() {
    private val _logsState = MutableStateFlow<ViewState<List<HitchLogUi>>>(ViewState.Loading)

    val state: StateFlow<LogListUiState> = combine(
        _logsState,
        authService.currentUser
    ) { logsState, user ->
        LogListUiState(
            logsState = logsState,
            isAnonymousUser = user?.isAnonymous == true
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LogListUiState(
            logsState = ViewState.Loading,
            isAnonymousUser = false
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

}