package com.vljx.hawkspeed.ui.component.mapoverlay.models

import android.util.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.DpSize

/**
 * A data class that represents a loaded and customised car. Essentially, this is assembled from a car type and various other modifications.
 */
data class Car(
    val modelsBitmaps: List<ImageBitmap>,
    val tileSize: Size,
    val frameDisplaySize: DpSize
) {
    /**
     * Return the image bitmap for this car's body. This will be tinted if the car has its colour changed at all.
     */
    val body: ImageBitmap
        get() = modelsBitmaps.first()
}