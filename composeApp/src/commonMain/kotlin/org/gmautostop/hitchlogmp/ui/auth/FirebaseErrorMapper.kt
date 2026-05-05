package org.gmautostop.hitchlogmp.ui.auth

import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.error_email_already_in_use
import hitchlogmp.composeapp.generated.resources.error_invalid_credential
import hitchlogmp.composeapp.generated.resources.error_network_failed
import hitchlogmp.composeapp.generated.resources.error_operation_not_allowed
import hitchlogmp.composeapp.generated.resources.error_too_many_requests
import hitchlogmp.composeapp.generated.resources.error_unknown
import hitchlogmp.composeapp.generated.resources.error_user_disabled
import hitchlogmp.composeapp.generated.resources.error_user_not_found
import hitchlogmp.composeapp.generated.resources.error_weak_password
import hitchlogmp.composeapp.generated.resources.error_wrong_password
import org.gmautostop.hitchlogmp.ui.UiText

fun Exception.toAuthErrorUiText(): UiText {
    val message = this.message ?: return UiText.StringResourceRef(Res.string.error_unknown)
    return when {
        message.contains("email-already-in-use") -> 
            UiText.StringResourceRef(Res.string.error_email_already_in_use)
        message.contains("weak-password") -> 
            UiText.StringResourceRef(Res.string.error_weak_password)
        message.contains("user-not-found") -> 
            UiText.StringResourceRef(Res.string.error_user_not_found)
        message.contains("wrong-password") -> 
            UiText.StringResourceRef(Res.string.error_wrong_password)
        message.contains("invalid-credential") -> 
            UiText.StringResourceRef(Res.string.error_invalid_credential)
        message.contains("user-disabled") -> 
            UiText.StringResourceRef(Res.string.error_user_disabled)
        message.contains("too-many-requests") -> 
            UiText.StringResourceRef(Res.string.error_too_many_requests)
        message.contains("network-request-failed") -> 
            UiText.StringResourceRef(Res.string.error_network_failed)
        message.contains("operation-not-allowed") -> 
            UiText.StringResourceRef(Res.string.error_operation_not_allowed)
        else -> UiText.StringResourceRef(Res.string.error_unknown)
    }
}
