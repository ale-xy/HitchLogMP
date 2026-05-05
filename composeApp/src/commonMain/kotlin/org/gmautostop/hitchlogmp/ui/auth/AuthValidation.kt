package org.gmautostop.hitchlogmp.ui.auth

import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.error_empty_email
import hitchlogmp.composeapp.generated.resources.error_empty_password
import hitchlogmp.composeapp.generated.resources.error_invalid_email
import hitchlogmp.composeapp.generated.resources.error_password_too_short
import hitchlogmp.composeapp.generated.resources.error_passwords_dont_match
import org.gmautostop.hitchlogmp.ui.UiText

fun validateEmail(email: String): UiText? = when {
    email.isBlank() -> UiText.StringResourceRef(Res.string.error_empty_email)
    !email.matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) -> 
        UiText.StringResourceRef(Res.string.error_invalid_email)
    else -> null
}

fun validatePassword(password: String): UiText? = when {
    password.isBlank() -> UiText.StringResourceRef(Res.string.error_empty_password)
    password.length < 6 -> UiText.StringResourceRef(Res.string.error_password_too_short)
    else -> null
}

fun validatePasswordsMatch(password: String, confirmPassword: String): UiText? =
    if (password != confirmPassword) {
        UiText.StringResourceRef(Res.string.error_passwords_dont_match)
    } else null
