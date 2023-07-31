package com.vljx.hawkspeed.ui.component.mapoverlay.enums

import android.util.Size
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import com.vljx.hawkspeed.R

/**
 * An enum that contains the different kinds of car models that can be used.
 */
enum class CarType(
    @DrawableRes val bodyDrawableId: Int,
    @DrawableRes val maskDrawableId: Int?,
    @DimenRes val frameDisplaySizeId: Int,
    val tileSize: Size
) {
    DEFAULT(
        bodyDrawableId = R.drawable.defaultcar,
        maskDrawableId = null,
        frameDisplaySizeId = R.dimen.defaultcar_frame_size,
        tileSize = Size(19, 19)
    )
}