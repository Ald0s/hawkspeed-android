package com.vljx.hawkspeed.ui.component.mapoverlay.enums

import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import com.vljx.hawkspeed.R

enum class TrackMarkerType(
    @DrawableRes val trackDrawableId: Int,
    @DimenRes val frameDisplaySizeId: Int
) {
    DEFAULT(
        R.drawable.ic_route,
        R.dimen.track_frame_size
    )
}