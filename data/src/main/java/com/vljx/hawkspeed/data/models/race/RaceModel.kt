package com.vljx.hawkspeed.data.models.race

/**
 * A race model type that represents a serialised race view model, with options to comment, like etc.
 */
data class RaceModel(
    val raceUid: String,
    val trackUid: String,
    val started: Long,
    val finished: Long?,
    val isDisqualified: Boolean,
    val disqualificationReason: String?,
    val isCancelled: Boolean
)