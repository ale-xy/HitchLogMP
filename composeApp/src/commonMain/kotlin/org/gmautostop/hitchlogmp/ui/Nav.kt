package org.gmautostop.hitchlogmp.ui

import kotlinx.serialization.Serializable
import org.gmautostop.hitchlogmp.data.HitchLogRecordType

sealed interface Screen {

    @Serializable
    data object Auth : Screen

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
        val recordType: HitchLogRecordType = HitchLogRecordType.FREE_TEXT
    ) : Screen

}


