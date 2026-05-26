package org.gmautostop.hitchlogmp.domain.repository

import org.gmautostop.hitchlogmp.domain.AppError

sealed class Response<out T> {
    class Loading<out T>: Response<T>()

    data class Success<out T>(
        val data: T
    ): Response<T>()

    data class Failure<out T>(
        val error: AppError
    ): Response<T>()
}
