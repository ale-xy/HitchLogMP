package org.gmautostop.hitchlogmp.domain

sealed class AppError {
    abstract val displayMessage: String

    data object NotAuthenticated : AppError() {
        override val displayMessage = "Not authenticated"
    }
    data object NotFound : AppError() {
        override val displayMessage = "Not found"
    }
    data class NetworkError(val message: String) : AppError() {
        override val displayMessage get() = message
    }
    data class ParseError(val field: String) : AppError() {
        override val displayMessage get() = "Invalid $field"
    }
}
