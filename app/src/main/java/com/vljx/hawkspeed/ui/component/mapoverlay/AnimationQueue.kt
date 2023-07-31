package com.vljx.hawkspeed.ui.component.mapoverlay

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.LinkedList
import java.util.Queue

/**
 * A queue class for executing world object animations in order.
 */
class AnimationQueue(
    private val scope: CoroutineScope,
    private val animateToPosition: suspend ((PositionUpdate) -> Unit)
) {
    // A data class for containing a coordinate pair and calculated duration.
    data class PositionUpdate(
        val updateIndex: Int,
        val coordinate: LatLng,
        val rotation: Float,
        val duration: Long
    )
    private val positionQueue: Queue<PositionUpdate> = LinkedList()
    private val mutex: Mutex = Mutex()
    // Save an index for tracking number of animations enqueued.
    private var currentTopIndex: Int = 0
    // Index of the update currently being worked on.
    private var currentExecutingIndex: Int = 0
    // Save the current timestamp in milliseconds.
    private var timestampLastUpdate: Long = System.currentTimeMillis()

    /**
     * Non-suspending function that will add a coordinate to the position queue. If the queue is empty, animation toward this position will begin immediately, otherwise,
     * it will be queued and executed after the position preceding it.
     */
    fun enqueueNewPosition(
        newCoordinate: LatLng,
        newRotation: Float,
        loggedAt: Long
    ) {
        // Create a new position update.
        val positionUpdate = PositionUpdate(
            updateIndex = currentTopIndex++,
            coordinate = newCoordinate,
            rotation = newRotation,
            duration = durationWithCatchup(System.currentTimeMillis() - timestampLastUpdate)
        )
        // Update last updated logged at.
        timestampLastUpdate = System.currentTimeMillis()
        // Enqueue the update.
        positionQueue.add(positionUpdate)
        Timber.d("Enqueued new position update! Index is ${positionUpdate.updateIndex}")
        // Now, call run animate next on the given scope.
        scope.launch {
            animateNext()
        }
    }

    /**
     * Clear the current queue as it is and set index to 0. Call this from on dispose callbacks or if the world object is not visible.
     */
    fun clearQueue() {
        Timber.d("Clearing animation queue and resetting index.")
        // Attempt to cancel scope.
        try {
            scope.cancel()
        } catch(ise: IllegalStateException) {
            // Do nothing.
            Timber.w(ise)
        }
        // Attempt to unlock mutex now that job is gone.
        try {
            mutex.unlock()
        } catch(ise: IllegalStateException) {
            // Do nothing.
            Timber.w(ise)
        }
        positionQueue.clear()
        timestampLastUpdate = System.currentTimeMillis()
        currentExecutingIndex = 0
        currentTopIndex = 0
    }

    /**
     * A suspending function that will continuously iterate through the current backlog of required positions and execute the proceeding.
     */
    private suspend fun animateNext() {
        try {
            mutex.withLock {
                while(positionQueue.isNotEmpty()) {
                    // Dequeue a value from the positions queue.
                    val nextUpdate = positionQueue.remove()
                    currentExecutingIndex = nextUpdate.updateIndex
                    Timber.d("Running animation ${nextUpdate.updateIndex}:\nPosition: ${nextUpdate.coordinate.latitude}, ${nextUpdate.coordinate.longitude}\nRotation: ${nextUpdate.rotation.toInt()}\nDuration: ${nextUpdate.duration}")
                    // Call the given animate to position function.
                    animateToPosition(nextUpdate)
                    Timber.d("Animation ${nextUpdate.updateIndex} complete.")
                }
            }
        } catch (ise: IllegalStateException) {
            // Do nothing.
            Timber.w(ise)
        }
    }

    /**
     * Adjust the given duration such that, if there's a gap between current index and highest index of queued updates, the duration is greatly
     * reduced to play catch up.
     */
    private fun durationWithCatchup(
        proposedDuration: Long
    ): Long {
        // We'll be very aggressive. If difference between is greater than or equal to 4, we'll subtract 80% of proposed duration.
        if(currentTopIndex - currentExecutingIndex >= 4) {
            return proposedDuration - (0.8f * proposedDuration).toLong()
        }
        return proposedDuration
    }
}