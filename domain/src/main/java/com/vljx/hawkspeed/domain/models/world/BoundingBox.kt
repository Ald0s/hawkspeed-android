package com.vljx.hawkspeed.domain.models.world

import android.graphics.RectF

data class BoundingBox(
    val southWest: Coordinate,
    val northEast: Coordinate
) {
    /**
     *
     */
    fun toRectangle(): RectF =
        RectF(
            this.southWest.longitude.toFloat(),
            this.southWest.latitude.toFloat(),
            this.northEast.longitude.toFloat(),
            this.northEast.latitude.toFloat()
        )
}