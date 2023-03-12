package com.vljx.hawkspeed.view.base

import android.content.*
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.CallSuper
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewbinding.ViewBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.WorldService
import com.vljx.hawkspeed.models.world.Viewport
import com.vljx.hawkspeed.models.world.WorldInitial
import com.vljx.hawkspeed.util.Extension.getEnumExtra
import timber.log.Timber

/**
 * A base fragment that has a dependency on a Google Maps fragment, and on the WorldService.
 */
abstract class BaseWorldMapFragment<ViewBindingCls: ViewBinding>: BaseFragment<ViewBindingCls>(),
    OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    protected lateinit var mWorldService: WorldService
    protected var isServiceBound: Boolean = false
    protected var googleMap: GoogleMap? = null

    private lateinit var worldBroadcastReceiver: WorldBroadcastReceiver

    protected abstract fun getSupportMapFragment(): SupportMapFragment

    private val serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            val binder = service as WorldService.WorldServiceBinder
            mWorldService = binder.getService()
            isServiceBound = true
            // As soon as the world service is bound, we will begin the load of the map.
            Timber.d("Beginning load of map reference NOW!")
            // Start the async map load.
            getSupportMapFragment().getMapAsync(this@BaseWorldMapFragment)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        worldBroadcastReceiver = WorldBroadcastReceiver()
        arguments?.let {

        }
    }

    override fun onStart() {
        // Bind to the service.
        requireActivity().bindService(
            Intent(requireContext(), WorldService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
        // Register all receivers for world updates.
        LocalBroadcastManager.getInstance(requireContext()).apply {
            registerReceiver(worldBroadcastReceiver, IntentFilter(WorldService.ACTION_WORLD_STATUS))
        }
        super.onStart()
    }

    @CallSuper
    override fun onMapReady(p0: GoogleMap) {
        this.googleMap = p0
        // As soon as the map is ready, we want to attach a camera move listener that will continually update the device's most recent viewport.
        googleMap?.setOnCameraMoveListener {
            try {
                // Whenever camera is moved, we'll get the current viewport.
                val viewport: Viewport = getCurrentViewport()
                    ?: throw Exception("setOnCameraMoveListener raised; failed to get current viewport, googleMap is probably null.")
                // With this viewport instance, update the viewport in the world service.
                mWorldService.setLatestViewport(viewport)
            } catch(e: Exception) {
                // For now, we'll just print a warning for whatever this error is, as the google map may not always be available.
                Timber.w(e)
            }
        }
        // Whenever the camera is idle, we want to forcibly invoke a viewport update, which will cause a query for world objects within that viewport.
        googleMap?.setOnCameraIdleListener {
            // TODO: get the viewport, as in camera move listener, but force a query for updated world objects in view.
        }
    }

    override fun onStop() {
        // We'll unbind from the service on stop.
        requireActivity().unbindService(serviceConnection)
        // Unregister all receivers for world updates.
        LocalBroadcastManager.getInstance(requireContext()).apply {
            unregisterReceiver(worldBroadcastReceiver)
        }
        super.onStop()
    }

    protected fun getCurrentViewport(): Viewport? {
        return googleMap?.run {
            val visibleRegion = projection.visibleRegion
            Viewport(
                visibleRegion.latLngBounds.southwest.longitude, visibleRegion.latLngBounds.southwest.latitude,
                visibleRegion.latLngBounds.northeast.longitude, visibleRegion.latLngBounds.northeast.latitude,
                cameraPosition.zoom
            )
        }
    }

    protected fun moveCamera(latLng: LatLng, zoom: Float, bearing: Float) {
        googleMap?.apply {
            moveCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition(
                        latLng,
                        zoom,
                        0f,
                        bearing
                    )
                )
            )
        }
    }

    protected open fun worldInitialReceived(worldInitial: WorldInitial) {

    }

    protected open fun newLocationReceived(location: Location) {

    }

    private inner class WorldBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                when(val status: WorldService.WorldStatus? = intent?.getEnumExtra<WorldService.WorldStatus>(
                    WorldService.ARG_WORLD_STATUS
                )) {
                    WorldService.WorldStatus.JOINED -> {
                        val worldInitial: WorldInitial = intent.getParcelableExtra(WorldInitial.ARG_WORLD_INITIAL)
                            ?: throw NullPointerException("No InitialWorld instance sent from service.")
                        worldInitialReceived(worldInitial)
                    }
                    WorldService.WorldStatus.LOCATION -> {
                        val location: Location = intent.extras?.getParcelable(WorldService.ARG_LOCATION)
                            ?: throw NullPointerException("No Location instance sent from service.")
                        newLocationReceived(location)
                    }
                    else -> { throw NotImplementedError("No such world service status: $status") }
                }
            } catch(e: Exception) {
                Timber.e(e)
            }
        }
    }
}