package com.vljx.hawkspeed.view.base

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Bundle
import androidx.databinding.ViewDataBinding
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.vljx.hawkspeed.WorldService
import com.vljx.hawkspeed.models.world.WorldInitial
import com.vljx.hawkspeed.util.Extension.getEnumExtra
import timber.log.Timber

/**
 * A base fragment type that is locked onto following the device; both by position and bearing in a strict birds eye format.
 */
abstract class BaseFollowWorldMapFragment<ViewBindingCls: ViewDataBinding>: BaseWorldMapFragment<ViewBindingCls>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("MissingPermission") // TODO: suppressing MissingPermission warning, probably not a good idea.
    override fun onMapReady(p0: GoogleMap) {
        super.onMapReady(p0)
        // Configure the map for following the device. For this, we will lock the view to the device's position, zoom to 17.5, and tilt to straight down.
        p0.isMyLocationEnabled = true
        p0.setMaxZoomPreference(17.5f)
        p0.setMinZoomPreference(17.5f)
    }

    override fun newLocationReceived(location: Location) {
        // When we receive a location update event, we will move the camera to follow the location.
        googleMap?.apply {
            Timber.d("Location: $location")
            // TODO: animateCamera instead of moveCamera.
            moveCamera(
                LatLng(location.latitude, location.longitude),
                17.5f,
                location.bearing
            )
        }
    }

    /*override fun worldInitialReceived(worldInitial: WorldInitial) {
        // Move the camera to the initial point, locked at 17.5f zoom.
        moveCamera(LatLng(worldInitial.latitude, worldInitial.longitude), 17.5f, worldInitial.rotation)
    }

    */
}