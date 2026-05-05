package org.gmautostop.hitchlogmp.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.gmautostop.hitchlogmp.awaitFirestorePendingWrites
import org.lighthousegames.logging.logging

/**
 * Tracks pending Firestore writes using the native waitForPendingWrites() API.
 *
 * When a write is initiated, trackWrite() sets hasPendingWrites to true and launches
 * a coroutine that calls awaitFirestorePendingWrites(). This suspends until the server
 * acknowledges all pending writes, then clears the flag.
 *
 * This approach is accurate regardless of whether snapshots are received, because it
 * directly queries the Firestore SDK's internal write queue state.
 */
class FirestoreSyncTracker {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val hasPendingWrites: StateFlow<Boolean>
        field = MutableStateFlow(false)

    /**
     * Call immediately after initiating a write operation.
     * Sets the pending flag and waits for server acknowledgment.
     */
    fun trackWrite() {
        hasPendingWrites.value = true
        scope.launch {
            try {
                awaitFirestorePendingWrites()
                hasPendingWrites.value = false
            } catch (e: Exception) {
                // stays true — reset() will be called on sign-out
                log.w(err = e) { "awaitFirestorePendingWrites failed: ${e.message}" }
            }
        }
    }

    /** Reset on sign-out or fresh start. */
    fun reset() {
        hasPendingWrites.value = false
    }

    companion object {
        val log = logging()
    }
}
