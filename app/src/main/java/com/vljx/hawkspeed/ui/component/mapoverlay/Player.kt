package com.vljx.hawkspeed.ui.component.mapoverlay

import android.graphics.Point
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import com.google.android.gms.maps.model.LatLng
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.models.world.CurrentPlayer
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.ui.component.mapoverlay.enums.CarType
import com.vljx.hawkspeed.ui.component.mapoverlay.models.Car

/**
 * Draw the current player to the map overlay.
 */
@Composable
fun MapOverlayScope.DrawCurrentPlayer(
    currentPlayer: CurrentPlayer,
    isBeingFollowed: Boolean,
    onNewCameraPosition: ((LatLng, Float) -> Unit)? = null
) {
    // For now, we only have the one vehicle. So we'll just support that one.
    val carType = CarType.DEFAULT
    // From this car type, load both the body and mask image bitmaps.
    val carBitmaps = listOf(
        ImageBitmap.imageResource(id = R.drawable.defaultcar)
    )
    // From this car type, load the desired frame size.
    val carFrameDisplaySize = DpSize(
        width = dimensionResource(id = carType.frameDisplaySizeId),
        height = dimensionResource(id = carType.frameDisplaySizeId)
    )
    // Build a loaded car model from this.
    val carToUse = Car(
        carBitmaps,
        carType.tileSize,
        carFrameDisplaySize
    )

    DrawPlayer(
        playerPosition = currentPlayer.playerPosition,
        car = carToUse,
        isBeingFollowed = isBeingFollowed,
        onNewCameraPosition = onNewCameraPosition
    )
}

/**
 * Draw a player, given a loaded car instance.
 *
 * TODO: code review here at some stage.
 * I believe I perform a backwards write to currentPositionRotation state, it doesn't seem to cause huge issues, perhaps because all reads/changes done
 * are done outside of composition, and in another thread.
 */
@Composable
fun MapOverlayScope.DrawPlayer(
    playerPosition: PlayerPosition,
    car: Car,
    isBeingFollowed: Boolean,
    onNewCameraPosition: ((LatLng, Float) -> Unit)? = null
) {
    // Get the pixel size for the frame display size.
    val frameSizePx: IntSize = remember {
        car.frameDisplaySize
            .toPixelSize()
    }
    var currentPositionRotation: Pair<LatLng, Float> by remember {
        mutableStateOf(Pair(LatLng(playerPosition.latitude, playerPosition.longitude), playerPosition.bearing))
    }
    // Now, calculate and save the initial world object scale based on camera zoom.
    var worldObjectScale: Float = remember {
        getWorldObjectScale()
    }
    // Calculate and save the initial position of the world object on screen from coordinates on the map.
    var positionOnScreen: Point? = remember {
        LatLng(playerPosition.latitude, playerPosition.longitude).calculatePositionOnScreen()
    }
    // Save a nullable point for the current calculated frame size.
    var calculatedFrameSize: IntSize? by remember {
        mutableStateOf(null)
    }

    // Remember a coroutine scope here for this car.
    val scope = rememberCoroutineScope()
    // Remember an animatable from 0..1, we'll use these frame values for linear interpolation for position & rotation anims concurrently.
    val dualAnimator = remember { Animatable(0f) }

    suspend fun animateToPosition(
        positionUpdate: AnimationQueue.PositionUpdate
    ) {
        // Set dual animator to 0.
        dualAnimator.snapTo(0f)
        // Now, animate to 1 with given duration.
        dualAnimator.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = positionUpdate.duration.toInt()
            )
        ) {
            if(value <= 1f) {
                val interpolatedPosition = LatLngInterpolator.Linear.interpolate(
                    value,
                    LatLng(currentPositionRotation.first.latitude, currentPositionRotation.first.longitude),
                    LatLng(positionUpdate.coordinate.latitude, positionUpdate.coordinate.longitude)
                )
                val interpolatedRotation = RotationInterpolator.Linear.interpolate(
                    value,
                    currentPositionRotation.second,
                    positionUpdate.rotation
                )
                onNewCameraPosition?.invoke(interpolatedPosition, interpolatedRotation)
                currentPositionRotation = Pair(interpolatedPosition, interpolatedRotation)
            }
        }
    }

    fun snapToPosition(
        positionUpdate: AnimationQueue.PositionUpdate
    ) {
        // Now, update the current coordinate, rotation, the state that recomposes for them both and finally call the new camera position callback.
        currentPositionRotation = Pair(positionUpdate.coordinate, positionUpdate.rotation)
        onNewCameraPosition?.invoke(positionUpdate.coordinate, positionUpdate.rotation)
    }

    // Remember an animation queue here.
    val animationQueue: AnimationQueue by remember {
        mutableStateOf(AnimationQueue(scope, ::animateToPosition, ::snapToPosition))
    }

    // Launched effect on player position that will determine when to enqueue a position update for animation.
    LaunchedEffect(key1 = playerPosition, block = {
        // Get the location from and the location to.
        /*val fromLatLng = LatLng(currentPositionRotation.first.latitude, currentPositionRotation.first.longitude)
        val toLatLng = LatLng(playerPosition.latitude, playerPosition.longitude)
        // Calculate distance, in meters, from these two points. If they are greater than MAX_DISTANCE_CHANGE_SNAP, we will instead snap to this location.
        val distanceBetween = SphericalUtil.computeDistanceBetween(fromLatLng, toLatLng)
        if(distanceBetween > MAX_DISTANCE_CHANGE_SNAP) {
            Timber.d("Player will be snapped between $fromLatLng and $toLatLng, as distance between the two points is substantial (${distanceBetween}m).")
            // Clear animation queue, since we don't need this updates any longer.
            animationQueue.clearQueue()
            // Now, update the current coordinate, rotation, the state that recomposes for them both and finally call the new camera position callback.
            currentPositionRotation = Pair(toLatLng, playerPosition.bearing)
            onNewCameraPosition?.invoke(toLatLng, playerPosition.bearing)
        } else {
            animationQueue.enqueueNewPosition(
                LatLng(playerPosition.latitude, playerPosition.longitude),
                playerPosition.bearing,
                playerPosition.loggedAt
            )
        }*/

        // Attempt to enqueue a new update for this object. Animation queue will handle filtering out both extreme cases of change.
        animationQueue.enqueueNewPosition(
            LatLng(playerPosition.latitude, playerPosition.longitude),
            playerPosition.bearing,
            playerPosition.loggedAt
        )
    })

    worldObjectScale = getWorldObjectScale()

    // Only draw the actual car if we have a valid world object absolute offset.
    calculateObjectAbsoluteOffset(
        currentPositionRotation.first.calculatePositionOnScreen(),
        calculatedFrameSize
    )?.let { offsetPixels ->
        Box(
            modifier = Modifier
                .absoluteOffset { offsetPixels }
                .onSizeChanged { size ->
                    calculatedFrameSize = size
                }
                .graphicsLayer {
                    // Set the correct scale.
                    scaleX = worldObjectScale
                    scaleY = worldObjectScale
                }
                .size(car.frameDisplaySize)
                .clip(RectangleShape)
        ) {
            // Calculate the top left coordinate for the desired angle of rotation.
            val targetFramePosition = currentPositionRotation.second.toInt().tileForDegree(
                tileSize = car.tileSize,
                frameSizePixels = frameSizePx
            )

            Canvas(
                modifier = Modifier
                    .graphicsLayer {
                        // Set translation to show the correct frame of our animation.
                        translationX = -targetFramePosition.x.toFloat()
                        translationY = -targetFramePosition.y.toFloat()
                    },
                onDraw = {
                    // Apply a rotation relative to camera bearing here to ensure the world object does not rotate when camera rotates.
                    rotate(
                        degrees = getObjectRotation(),
                        pivot = Offset(
                            x = (targetFramePosition.x + (frameSizePx.width / 2)).toFloat(),
                            y = (targetFramePosition.y + (frameSizePx.height / 2)).toFloat()
                        )
                    ) {
                        // Draw the bitmap image for the vehicle's body, resizing the spritesheet image itself to match the explicit number of tiles on X and Y.
                        drawImage(
                            image = car.body,
                            dstSize = IntSize(
                                width = frameSizePx.width * car.tileSize.width,
                                height = frameSizePx.height * car.tileSize.height
                            )
                        )
                    }
                }
            )
        }
    }
}