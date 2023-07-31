package com.vljx.hawkspeed

import android.annotation.SuppressLint
import android.graphics.RectF
import android.location.Location
import android.location.LocationRequest
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_ENTER
import com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_EXIT
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.VisibleRegion
import com.google.android.gms.tasks.Tasks
import com.google.maps.android.SphericalUtil
import com.vljx.hawkspeed.domain.models.world.BoundingBox
import com.vljx.hawkspeed.domain.models.world.Coordinate
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.models.world.PlayerPositionWithOrientation
import timber.log.Timber
import java.util.concurrent.ExecutionException
import kotlin.math.roundToInt

object Extension {
    const val TILT_DEFAULT = 45f

    const val TRACK_OVERVIEW_PADDING = 100

    const val FOLLOW_PLAYER_ZOOM = 20f
    const val FOLLOW_PLAYER_TILT = 45f

    /**
     * Function that will accept a specific detected activity, and setup a separate activity transition for both enter and exit.
     */
    fun MutableList<ActivityTransition>.addDetectedActivity(detectedActivityId: Int): MutableList<ActivityTransition> =
        this.also {
            it.addAll(
                listOf(
                    ActivityTransition.Builder()
                        .setActivityType(detectedActivityId)
                        .setActivityTransition(ACTIVITY_TRANSITION_ENTER)
                        .build(),
                    ActivityTransition.Builder()
                        .setActivityType(detectedActivityId)
                        .setActivityTransition(ACTIVITY_TRANSITION_EXIT)
                        .build()
                )
            )
        }


    /**
     * Get the location availability and return it. This will run the task blocking.
     */
    @SuppressLint("MissingPermission")
    suspend fun FusedLocationProviderClient.getCurrentLocationAvailability(): LocationAvailability {
        // Get the most recent location availability status.
        try {
            // We'll check our location availability now.
            return Tasks.await(
                this.locationAvailability
            )
        } catch (ee: ExecutionException) {
            // TODO: exception occurred whilst running task.
            // TODO: This is the same exception we'd get in the handler.
            Timber.e(ee)
            throw NotImplementedError("Getting location availability failed! ANd is also not implemented.")
        } catch (ie: InterruptedException) {
            Timber.w("Getting location availability was interrupted!")
            // TODO: implement a proper handler here.
            throw NotImplementedError("Failed to get location availability because it was interrupted - this is not yet handled.")
        }
    }

    /**
     * Run a blocking get current location call for a Location instance matching the settings given by the location request. This function will
     * either return the desired location, or will throw an exception.
     */
    @SuppressLint("MissingPermission")
    suspend fun FusedLocationProviderClient.getCurrentLocation(
        currentLocationRequest: CurrentLocationRequest
    ): Location {
        val location: Location
        try {
            // Await the query for the current location.
            location = Tasks.await(
                getCurrentLocation(
                    currentLocationRequest,
                    null
                )
            )
            // Return the location.
            return location
        } catch (ee: ExecutionException) {
            // TODO: exception occurred whilst running task.
            // TODO: This is the same exception we'd get in the handler.
            Timber.e(ee)
            throw NotImplementedError("Getting current location failed! ANd is also not implemented.")
        } catch (it: InterruptedException) {
            Timber.w("Getting current location was interrupted!")
            // TODO: implement a proper handler here.
            throw NotImplementedError("Failed to get current location because it was interrupted - this is not yet handled.")
        }
    }

    /**
     * An extension function for producing a camera position from a player position.
     */
    fun PlayerPosition.toFollowCameraPosition(
        zoom: Float = FOLLOW_PLAYER_ZOOM,
        tilt: Float = FOLLOW_PLAYER_TILT
    ): CameraPosition =
        CameraPosition.builder()
            .target(LatLng(latitude, longitude))
            .tilt(tilt)
            .bearing(bearing)
            .zoom(zoom)
            .build()

    /**
     * An extension function for producing a camera position from a player position with orientation.
     * TODO: this function SHOULD utilise data provided by the orientation.
     */
    fun PlayerPositionWithOrientation.toFollowCameraPosition(
        zoom: Float = FOLLOW_PLAYER_ZOOM,
        tilt: Float = FOLLOW_PLAYER_TILT
    ): CameraPosition =
        CameraPosition.builder()
            .target(LatLng(position.latitude, position.longitude))
            .tilt(tilt)
            .bearing(position.bearing)
            .zoom(zoom)
            .build()

    /**
     * An extension function that will generate a target repositioning of the map's camera such that the subject bounding box is the focus of the viewport.
     */
    fun BoundingBox.toOverviewCameraUpdate(
        padding: Int = TRACK_OVERVIEW_PADDING
    ): CameraUpdate =
        CameraUpdateFactory.newLatLngBounds(
            LatLngBounds(
                LatLng(southWest.latitude, southWest.longitude),
                LatLng(northEast.latitude, northEast.longitude)
            ),
            padding
        )

    /**
     * An extension function that will generate a camera update based on the subject camera position, but with no tilt.
     */
    fun CameraPosition.noTilt(): CameraUpdate =
        CameraUpdateFactory.newCameraPosition(
            CameraPosition(
                this.target,
                this.zoom,
                0f,
                this.bearing
            )
        )

    /**
     * An extension function to a list of coordinates that will return the total length, prettified like; 2.4km / 183m.
     * TODO: move this elsewhere, perhaps to a data class that can also produce this information.
     */
    fun List<LatLng>.prettyLength(): String =
        let { latLngs -> SphericalUtil.computeLength(latLngs).roundToInt() }
        .let { totalLength ->
            if((totalLength / 1000).toInt() > 0) {
                return "${String.format("%.2f", totalLength / 1000)}km"
            }
            return "${totalLength}m"
        }

    /**
     * Return a bounding box for the given visible region's latlng bounds.
     */
    fun VisibleRegion.toBoundingBox(): BoundingBox =
        BoundingBox(
            Coordinate(
                this.latLngBounds.southwest.latitude,
                this.latLngBounds.southwest.longitude
            ),
            Coordinate(
                this.latLngBounds.northeast.latitude,
                this.latLngBounds.northeast.longitude
            )
        )

    /**
     * Determine if a bounding box overlaps at all with the given bounding box.
     */
    fun BoundingBox.overlapsWith(boundingBox: BoundingBox): Boolean =
        this.toRectangle()
            .intersect(boundingBox.toRectangle())
}