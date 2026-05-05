package org.gmautostop.hitchlogmp.ui.auth

import dev.gitlive.firebase.auth.FirebaseAuthException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidCredentialsException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidUserException
import dev.gitlive.firebase.auth.FirebaseAuthRecentLoginRequiredException
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import dev.gitlive.firebase.auth.FirebaseAuthWeakPasswordException
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.error_email_already_in_use
import hitchlogmp.composeapp.generated.resources.error_invalid_credential
import hitchlogmp.composeapp.generated.resources.error_network_failed
import hitchlogmp.composeapp.generated.resources.error_operation_not_allowed
import hitchlogmp.composeapp.generated.resources.error_too_many_requests
import hitchlogmp.composeapp.generated.resources.error_unknown
import hitchlogmp.composeapp.generated.resources.error_user_not_found
import hitchlogmp.composeapp.generated.resources.error_weak_password
import org.gmautostop.hitchlogmp.ui.UiText

fun Exception.toAuthErrorUiText(): UiText {
    return when (this) {
        // Specific exception types
        is FirebaseAuthInvalidCredentialsException -> 
            UiText.StringResourceRef(Res.string.error_invalid_credential)
        
        is FirebaseAuthInvalidUserException -> 
            UiText.StringResourceRef(Res.string.error_user_not_found)
        
        is FirebaseAuthUserCollisionException -> 
            UiText.StringResourceRef(Res.string.error_email_already_in_use)
        
        is FirebaseAuthWeakPasswordException -> 
            UiText.StringResourceRef(Res.string.error_weak_password)
        
        is FirebaseAuthRecentLoginRequiredException -> 
            UiText.StringResourceRef(Res.string.error_invalid_credential)
        
        // Base FirebaseAuthException - check message for specific cases
        is FirebaseAuthException -> {
            val message = this.message?.lowercase() ?: ""
            when {
                message.contains("network") || message.contains("connection") -> 
                    UiText.StringResourceRef(Res.string.error_network_failed)
                message.contains("too many") || message.contains("quota") -> 
                    UiText.StringResourceRef(Res.string.error_too_many_requests)
                message.contains("disabled") || message.contains("operation-not-allowed") -> 
                    UiText.StringResourceRef(Res.string.error_operation_not_allowed)
                else -> 
                    UiText.StringResourceRef(Res.string.error_unknown)
            }
        }
        
        // Non-Firebase exceptions
        else -> UiText.StringResourceRef(Res.string.error_unknown)
    }
}
