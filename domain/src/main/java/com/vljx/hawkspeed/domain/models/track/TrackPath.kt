package com.vljx.hawkspeed.domain.models.track

import android.os.Parcelable
import com.vljx.hawkspeed.domain.models.world.BoundingBox
import com.vljx.hawkspeed.domain.models.world.Coordinate
import com.vljx.hawkspeed.domain.thirdparty.BoundingBoxUtil
import kotlinx.parcelize.Parcelize
import java.lang.IndexOutOfBoundsException

@Parcelize
data class TrackPath(
    val trackPathUid: String,
    val points: List<TrackPoint>
): Parcelable {
    /**
     * Return a bounding box from all points in this path
     */
    fun getBoundingBox(): BoundingBox {
        return BoundingBoxUtil.boundingBoxFrom(
            points.map { Coordinate(it.latitude, it.longitude) }
        )
    }
}