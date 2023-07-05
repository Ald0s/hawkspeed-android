package com.vljx.hawkspeed.domain

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object Extension {
    /**
     * An extension function to the Kotlin Duration that will allow us to prettify a time such that milliseconds becomes; 03:23:561
     */
    fun Duration.toRaceTime() =
        this.toComponents { minutes, seconds, nanoseconds ->
            // Determine milliseconds from the nanoseconds argument.
            val milliseconds = nanoseconds
                .toDuration(DurationUnit.NANOSECONDS)
                .inWholeMilliseconds
            "$minutes:$seconds:$milliseconds"
        }
}