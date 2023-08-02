package com.vljx.hawkspeed.ui.component.mapoverlay

import android.graphics.Point
import android.util.Size
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import kotlin.math.absoluteValue

/**
 * A scope that provides functionality for calculating values based on the state of the camera and current density.
 */
class MapOverlayScope(
    val cameraPositionState: CameraPositionState,
    private val density: Density
) {
    /**
     *
     */
    fun getCurrentCameraPositionState(): CameraPositionState =
        cameraPositionState

    /**
     * Calculate a rotation offset to apply to the bitmap image. This should be used to ensure the camera can rotate without rotating
     * other objects that have a set bearing.
     */
    @Stable
    fun getObjectRotation(): Float =
        (FULL_ANGLE - cameraPositionState.position.bearing)

    /**
     * Determine the origin coordinates of a tile for a specific degree, given the number of tiles and size (in pixels) of each tile.
     * TODO: move this to something spritesheet/3d model specific.
     */
    fun Int.tileForDegree(
        tileSize: Size,
        frameSizePixels: IntSize
    ): Point =
        Point(
            (this % tileSize.width).absoluteValue * frameSizePixels.width,
            (this / tileSize.height).absoluteValue * frameSizePixels.height
        )

    /**
     * Transform a point on the screen (X,Y) to an offset (in pixels) that should be used to position a map overlay world object on the overlay itself. Optionally, provide
     * a frame size if the object must be anchored to an origin.
     */
    @Stable
    fun calculateObjectAbsoluteOffset(
        positionOnScreen: Point?,
        calculatedFrameSize: IntSize?
    ): IntOffset? =
        positionOnScreen?.let { point ->
            IntOffset(
                (point.x - ((calculatedFrameSize?.width ?: 0) / 2)),
                (point.y - ((calculatedFrameSize?.height ?: 0) / 2))
            )
        }

    /**
     * Given a lat/long pair, calculate the position on screen (X,Y) for that pair. This will only succeed if the camera position at this time has a valid projection attribute
     * and will return null otherwise. The returned value is in pixels.
     */
    @Stable
    fun LatLng.calculatePositionOnScreen(): Point? =
        cameraPositionState.projection
            ?.toScreenLocation(this)

    /**
     * Based on the camera's current zoom, calculate a relative scale value to be applied to world objects.
     */
    @Stable
    fun getWorldObjectScale(): Float =
        (cameraPositionState.position.zoom / DEFAULT_REFERENCE_ZOOM)
            .coerceAtLeast(MIN_WORLD_OBJECT_SCALE)
            .coerceAtMost(MAX_WORLD_OBJECT_SCALE)

    /**
     * Convert a display size to pixel size.
     */
    @Stable
    fun DpSize.toPixelSize(): IntSize =
        IntSize(
            (this.width.value * density.density).toInt(),
            (this.height.value * density.density).toInt()
        )

    /**
     * Convert a pixel size to display size.
     */
    @Stable
    fun IntSize.toDpSize(): DpSize =
        DpSize(
            width = (this.width / density.density).dp,
            height = (this.height / density.density).dp
        )

    companion object {
        const val DEFAULT_REFERENCE_ZOOM = 11f

        const val FULL_ANGLE = 360
        const val MIN_WORLD_OBJECT_SCALE = 0.6f
        const val MAX_WORLD_OBJECT_SCALE = 2.0f
    }
}