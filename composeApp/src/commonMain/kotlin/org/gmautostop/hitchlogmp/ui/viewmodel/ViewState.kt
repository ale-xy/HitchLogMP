package org.gmautostop.hitchlogmp.ui.viewmodel

import org.gmautostop.hitchlogmp.domain.AppError

sealed class ViewState<out T: Any> {
    data object Loading : ViewState<Nothing>()
    class Show<out T: Any>(val value: T) : ViewState<T>()
    class Error(val error: AppError) : ViewState<Nothing>()
}