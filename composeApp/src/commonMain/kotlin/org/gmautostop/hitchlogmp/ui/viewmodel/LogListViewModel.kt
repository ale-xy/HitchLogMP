package org.gmautostop.hitchlogmp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.gmautostop.hitchlogmp.data.HitchLog
import org.gmautostop.hitchlogmp.domain.Repository
import org.gmautostop.hitchlogmp.domain.Response
import org.lighthousegames.logging.logging

class LogListViewModel(repository: Repository): ViewModel() {
    private val _state = MutableStateFlow<ViewState<List<HitchLog>>>(ViewState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = ViewState.Loading

            repository.getLogs().distinctUntilChanged()
                .collect { response ->
                    _state.value = when(response) {
                        is Response.Loading -> ViewState.Loading
                        is Response.Failure -> ViewState.Error(response.errorMessage)
                        is Response.Success -> ViewState.Show(response.data)
                    }
                }
        }
    }

    companion object {
        val logger = logging()
    }
}