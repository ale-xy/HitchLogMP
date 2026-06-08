package org.gmautostop.hitchlogmp.domain.model

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.Serializable

@Serializable
data class HitchLog(
    var id: String = "",
    val userId: String = "",
    val raceId: String = "",
    val teamId: String = "",
    val name: String = "",
    val creationTime: Timestamp = Timestamp.now(),
    val team: String? = null,
    val comment: String? = null,
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
)
