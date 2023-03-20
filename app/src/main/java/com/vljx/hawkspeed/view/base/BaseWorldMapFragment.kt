package com.vljx.hawkspeed.view.base

import android.content.*
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.view.View
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewbinding.ViewBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.vljx.hawkspeed.BuildConfig
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.WorldService
import com.vljx.hawkspeed.data.socket.WorldSocketSession
import com.vljx.hawkspeed.data.socket.WorldSocketState
import com.vljx.hawkspeed.models.world.Viewport
import com.vljx.hawkspeed.models.world.WorldInitial
import com.vljx.hawkspeed.util.Extension.getEnumExtra
import com.vljx.hawkspeed.view.world.WorldObjectManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * A base fragment that has a dependency on a Google Maps fragment, and on the WorldService.
 */
abstract class BaseWorldMapFragment<ViewBindingCls: ViewBinding>: BaseFragment<ViewBindingCls>(),
    OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {
    @Inject
    lateinit var worldSocketSession: WorldSocketSession

    protected lateinit var mWorldService: WorldService
    protected var isServiceBound: Boolean = false
    protected var googleMap: GoogleMap? = null

    // A world object manager to keep track of, and be responsible for the drawing of world objects to the map.
    protected val worldObjectManager = WorldObjectManager(null)

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
        super.onStart()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onMapClick(p0: LatLng) {
        if(!BuildConfig.USE_MOCK_LOCATION) {
            throw NotImplementedError()
        }
        val location = Location(LocationManager.GPS_PROVIDER)
        location.latitude = p0.latitude
        location.longitude = p0.longitude
        location.speed = 0.0f
        location.time = System.currentTimeMillis()
        location.bearing = 180.0f
        location.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        if(!isServiceBound) {
            throw NotImplementedError()
        }
        mWorldService.mockLocation(location)
    }

    @CallSuper
    override fun onMapReady(p0: GoogleMap) {
        // Set this google map instance.
        this.googleMap = p0
        // As soon as we load a google map instance, update the world object manager.
        worldObjectManager.setGoogleMap(p0)
        // Establish event handlers.
        googleMap?.setOnMapClickListener(this)
        googleMap?.setOnMarkerClickListener(this)
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
            try {
                // Now that the camera is idle, grab the current viewport.
                val viewport: Viewport = getCurrentViewport()
                    ?: throw Exception("setOnCameraIdleListener raised; failed to get current viewport, googleMap is probably null.")
                // With this viewport instance, invoke a viewport update request.
                mWorldService.sendViewportUpdate(viewport)
                /**
                 * TODO: watch this closely, and only remove this todo and the warning below when the following has been clarified:
                 * Each time a location update is triggered, we grab the latest viewport from the service, which is set on every camera movement in the service.
                 * Also on every viewport idle, a separate viewport update request is triggered. Will these double up bandwidth use?
                 */
                Timber.w("SENDING VIEWPORT UPDATE")
            } catch(e: Exception) {
                // For now, we'll just print a warning for whatever this error is, as the google map may not always be available.
                Timber.w(e)
            }
        }
        // Setup collections in this fragment.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Launch a new collection for the latest work state.
                launch {
                    worldSocketSession.worldSocketState.collectLatest { worldSocketState ->
                        when(worldSocketState) {
                            is WorldSocketState.Connected -> {
                                // If world socket state is connected, this is the initial location for our player, upon joining the world. Set their camera to
                                // the indicated position & rotation.
                                moveCamera(
                                    LatLng(worldSocketState.latitude, worldSocketState.longitude),
                                    17.5f,
                                    worldSocketState.rotation
                                )
                                // Now, once camera is moved, we will perform a viewport update, since we have now been centered over our Player.
                                val viewport: Viewport = getCurrentViewport()
                                    ?: throw NotImplementedError("worldSocketState.collectLatest failed! We could not get the current viewport!")
                                // Send a viewport update.
                                mWorldService.sendViewportUpdate(viewport)
                            }
                            else -> {

                            }
                        }
                    }
                }
                // Launch a new collection for the latest location.
                launch {
                    worldSocketSession.currentLocation.collectLatest { location ->
                        // if location is not null, call new location received.
                        if(location != null) {
                            newLocationReceived(location)
                        } else {
                            Timber.w("LOCATION IS NULL")
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        // We'll unbind from the service on stop.
        requireActivity().unbindService(serviceConnection)
        super.onStop()
    }

    override fun onDestroyView() {
        // View is being destroyed, remove all objects from this google map.
        worldObjectManager.onDestroyView()
        super.onDestroyView()
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

    protected open fun newLocationReceived(location: Location) {

    }
}