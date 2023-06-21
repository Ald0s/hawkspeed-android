package com.vljx.hawkspeed.data.models.race

/**
 * An update model that describes races, that is, the object being serialised is not a view model.
 */
data class RaceUpdateModel(
    val raceUid: String,
    val trackUid: String,
    val started: Long,
    val finished: Long?,
    val isDisqualified: Boolean,
    val disqualificationReason: String?,
    val isCancelled: Boolean
)