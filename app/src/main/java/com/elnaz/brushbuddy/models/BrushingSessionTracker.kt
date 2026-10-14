package com.elnaz.brushbuddy.models

import android.util.Log
import java.time.Duration
import java.time.Instant

class BrushingSessionTracker {
    var brushingState: BrushingStateEnum = BrushingStateEnum.NOT_BRUSHING
        private set
    var currentStatus: BrushingStatus = BrushingStatus.IDLE
        private set
    var previousStatus: BrushingStatus = BrushingStatus.IDLE
        private set
    var sessionStartTime: Instant? = null
        private set
    var stoppedTime: Instant? = null
        private set
    var accumulatedDuration: Duration = Duration.ZERO
        private set
    private var resultStartTime: Instant? = null
    companion object {
        private const val TAG = "BrushingSessionTracker"
        private val TEN_SECOND_RULE: Duration = Duration.ofSeconds(10)
    }
    /*
     * Brushing session tracking plan:
     *
     * 1. First BRUSHING
     *    → accumulated = 0
     *    → start timing
     *
     * 2. STOP
     *    → add brushed time to accumulated
     *
     * 3. Restart <= 10 sec
     *    → keep accumulated
     *    → start timing another segment
     *
     * 4. While BRUSHING:
     *    - Calculate BrushBuddy session time as:
     *      toothbrush brushingTimeSeconds - sessionStartCounter.
     *
     * 5. Restart > 10 sec
     *    → accumulated = 0
     *    → start a new session
     */
    /*
     *  NOT_BRUSHING
     *      if Toothbrush ON
     *          New state is EARLY
     *  EARLY
     *      If timer > 10s
     *          New state is BRUSHING
     *      If Toothbrush OFF
     *          New state is NOT_BRUSHING
     *  BRUSHING,
     *      If Toothbrush OFF
     *              NEW state is PAUSED
     *
     *  PAUSED
     *      if timer > 10s
     *          if total_brush_timer >= 30
     *              Increment STREAK
     *              new state is CONFIRMED_SESSION
     *          else
     *              new state is ABORTED_SESSION
     *      IF ON and timer <= 10
     *          New state is BRUSHING
     *
     *  ABORTED_SESSION,
     *      if timer > 2
     *          new state is NOT_BRUSHING
     *
     *  CONFIRMED_SESSION,
     *      if timer > 2
     *          new state is NOT_BRUSHING
     *
     */
    fun update(oralBAdvertisement: OralBAdvertisement) {
        val newStatus: BrushingStatus = oralBAdvertisement.brushingStatus
        if (newStatus == currentStatus) return // No state change
        // Save history
        previousStatus = currentStatus
        currentStatus = newStatus
        when (newStatus) {
            BrushingStatus.BRUSHING -> {
                brushingState = BrushingStateEnum.EARLY
                val lastStopped = stoppedTime
                // Evaluate the 10-second rule if returning from a temporary stop
                if (lastStopped != null && Duration.between(lastStopped, Instant.now()) <= TEN_SECOND_RULE) {
                    brushingState = BrushingStateEnum.BRUSHING
                    val gap = Duration.between(lastStopped, Instant.now())
                    Log.d(
                        TAG,
                        "Resume same session - gap=${gap.seconds}s, " +
                                "accumulated=${accumulatedDuration.seconds}s"
                    )
                    // Within 10 seconds: Resume existing session without incrementing counter
                    sessionStartTime = Instant.now()
                    stoppedTime = null
                } else {
                    // First brushing OR pause was too long -> new session
                    if (lastStopped == null) {
                        Log.d(TAG, "First brushing - new session")
                    }else {
                        val gap = Duration.between(lastStopped, Instant.now())

                        Log.d(
                            TAG,
                            "Gap=${gap.seconds}s > ${TEN_SECOND_RULE.seconds}s - new session"
                        )
                    }
                    accumulatedDuration = Duration.ZERO
                    sessionStartTime = Instant.now()
                    stoppedTime = null
                }
            }
            BrushingStatus.IDLE -> {
                    val now = Instant.now()
                    stoppedTime = now
                    brushingState = when (brushingState) {
                        BrushingStateEnum.EARLY -> BrushingStateEnum.NOT_BRUSHING
                        BrushingStateEnum.BRUSHING -> BrushingStateEnum.PAUSED
                        else -> brushingState
                    }
                    // Add the active time chunk to the accumulated duration
                    sessionStartTime?.let { start ->
                        val segmentDuration = Duration.between(start, now)
                        accumulatedDuration += segmentDuration
                        Log.d(
                            TAG,
                            "Brushing stopped - segment=${segmentDuration.seconds}s, " +
                                    "total=${accumulatedDuration.seconds}s"
                        )
                }
                sessionStartTime = null
            }
        }
    }

    // Check time-based state transitions
    fun checkState(now: Instant = Instant.now()) {
        if (brushingState == BrushingStateEnum.EARLY) {
            val start = sessionStartTime

            if (start != null &&
                Duration.between(start, now) >= TEN_SECOND_RULE) {
                brushingState = BrushingStateEnum.BRUSHING
            }
        }
        if (brushingState == BrushingStateEnum.PAUSED) {
            val stopped = stoppedTime

            if (stopped != null &&
                Duration.between(stopped, now) > TEN_SECOND_RULE) {

                brushingState = if (accumulatedDuration.seconds >= 30) {
                    BrushingStateEnum.CONFIRMED_SESSION
                } else {
                    BrushingStateEnum.ABORTED_SESSION
                }
                resultStartTime = now
            }
        }
        // Reset the session result after 2 seconds
        if (brushingState == BrushingStateEnum.ABORTED_SESSION ||
            brushingState == BrushingStateEnum.CONFIRMED_SESSION) {

            val start = resultStartTime

            if (start != null &&
                Duration.between(start, now) >= Duration.ofSeconds(2)) {

                brushingState = BrushingStateEnum.NOT_BRUSHING
                resultStartTime = null
            }
        }
    }
    fun getLiveSessionDuration(now: Instant = Instant.now()): Duration {
        val start = sessionStartTime
        return if (currentStatus == BrushingStatus.BRUSHING && start != null) {
            accumulatedDuration + Duration.between(start, now)
        } else {
            accumulatedDuration
        }
    }
}
