package org.gmautostop.hitchlogmp.ui

import kotlinx.serialization.Serializable
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType

sealed interface Screen {

    @Serializable
    data object Auth : Screen

    @Serializable
    data object EmailLogin : Screen

    @Serializable
    data object EmailRegister : Screen

    @Serializable
    data object ForgotPassword : Screen

    @Serializable
    data class ForgotPasswordSent(val email: String) : Screen

    @Serializable
    data object LogList : Screen

    @Serializable
    data class EditLog(val logId: String = "") : Screen

    @Serializable
    data class Log(val logId: String) : Screen

    @Serializable
    data class EditRecord(
        val logId: String,
        val recordId: String = "",
        val recordType: String = HitchLogRecordType.FREE_TEXT.name
    ) : Screen

}


