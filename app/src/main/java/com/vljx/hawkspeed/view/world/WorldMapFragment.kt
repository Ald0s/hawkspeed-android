package com.vljx.hawkspeed.view.world

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.vljx.hawkspeed.BuildConfig
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.WorldService
import com.vljx.hawkspeed.data.socket.WorldSocketState
import com.vljx.hawkspeed.databinding.FragmentWorldMapBinding
import com.vljx.hawkspeed.models.world.Viewport
import com.vljx.hawkspeed.presenter.world.WorldMapPresenter
import com.vljx.hawkspeed.view.base.BaseWorldMapFragment
import com.vljx.hawkspeed.viewmodel.world.WorldMapViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * A [com.vljx.hawkspeed.view.base.BaseWorldMapFragment] subclass. This fragment represents the primary world view, and will coordinate
 * the permission gathering for its display. Use the [WorldMapFragment.newInstance] factory method to create an instance of this fragment.
 */
@AndroidEntryPoint
class WorldMapFragment : BaseWorldMapFragment<FragmentWorldMapBinding>(), WorldMapPresenter {
    private val worldMapViewModel: WorldMapViewModel by viewModels()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentWorldMapBinding
        get() = FragmentWorldMapBinding::inflate

    // Access to the location client.
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    // Provides parameters for the location request.
    private lateinit var locationRequest: LocationRequest
    private lateinit var settingsClient: SettingsClient

    override fun getSupportMapFragment(): SupportMapFragment =
        childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup location client.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = WorldService.newLocationRequest()
        settingsClient = LocationServices.getSettingsClient(requireActivity())
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            mViewBinding.worldMapViewModel = worldMapViewModel
            mViewBinding.worldMapPresenter = this@WorldMapFragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup flow collections for our permissions/state observers.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Monitor any changes in offers to race a race.
                launch {
                    worldMapViewModel.trackCanBeRaced.collectLatest { track ->
                        if(track != null) {
                            Timber.d("We are able to race the following track; $track")
                            /**
                             * TODO: this is where we can display a pop up UI of some description offering a race for this Player.
                             * For example, display a modal bottom sheet dialog, which is collapsed at say, 15% of the screen, and will popup to review info about the trace and
                             * offer the UI on which we can accept an offer to race.
                             */
                        } else {
                            Timber.d("We are not able to race any track currently.")
                            /**
                             * TODO: clear any UI created by the track-not-none handler.
                             */
                        }
                    }
                }
                // Collect all currently cached tracks, and their paths if available, and continually update their presence on the world map.
                launch {
                    worldMapViewModel.tracksWithPaths.collectLatest { tracksWithPaths ->
                        // Update the world object manager with these tracks.
                        worldObjectManager.updateTracks(tracksWithPaths)
                    }
                }
                // Start collecting the latest location permissions state.
                launch {
                    worldMapViewModel.locationPermissionState.collectLatest {
                        // If location permission is granted completely, we will proceed to check the location settings.
                        when(it) {
                            is LocationPermissionState.AllGranted -> {
                                Timber.d("Location permission state changed to AllGranted, checking our settings state...")
                                // Go ahead and ensure location settings are appropriate.
                                ensureLocationSettingsCompatible()
                            }
                            else -> {
                                /**
                                 * TODO: otherwise, if location permission is not granted, or only partially granted, we want to browse from here to an error interface.
                                 */
                                throw NotImplementedError("LocationPermissionState not granted/partially granted is not yet handled!")
                            }
                        }
                    }
                }
                // Start collecting the latest location settings state.
                launch {
                    worldMapViewModel.locationSettingsState.collectLatest {
                        when(it) {
                            is LocationSettingsState.Appropriate -> {

                            }
                            is LocationSettingsState.NotAppropriate -> {
                                /**
                                 * TODO: otherwise, if location settings are not appropriate, we want to browse from here to an error interface.
                                 */
                                throw NotImplementedError("LocationSettingsState not appropriate is not yet handled!")
                            }
                        }
                    }
                }
            }
        }
        // Whenever the view is created, start the permissions/settings flow.
        resolveLocationPermission()
    }

    override fun makeNewTrackClicked() {
        if(BuildConfig.USE_MOCK_LOCATION) {
            // TODO: if we are using mock location, set the mock location once again.
            mWorldService.mockLocation(
                Location(LocationManager.GPS_PROVIDER).apply {
                    latitude = -37.757557
                    longitude = 144.958444
                    speed = 0f
                    bearing = 180f
                    time = System.currentTimeMillis()
                    elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                }
            )
        }
        // Navigate to the record destination.
        findNavController().navigate(R.id.action_destination_world_map_to_destination_record_track)
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        // Attempt to locate a track for this marker.
        worldObjectManager.findTrackWithMarker(p0)?.let { trackWithPath ->
            // Invoke a download for the path of this track.
            /**
             * TODO: With the result of the call to getTrackPath, open up a track preview for it as well.
             */
            worldMapViewModel.getTrackPath(trackWithPath.track)
        }
        return false
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap) {
        super.onMapReady(p0)
        try {
            // Set my location enabled.
            p0.isMyLocationEnabled = true
            // TODO: we can move this call to joinWorld to somewhere else, that does not depend on the world map loading.
            Timber.d("Map is now READY! We will now request that the world service initiate the connection procedure.")
            // Join the world when the map is ready.
            mWorldService.joinWorld()
        } catch(se: SecurityException) {
            // TODO: security exception raised, location permission is probably not granted, resolve location permissions again.
            resolveLocationPermission()
        }
    }

    /**
     * Check the current granted permissions for access to location. This will check whether fine location is granted, and if not, will attempt to show a
     * rationale explaining why permission is needed. Finally, this function will request permission be granted. The results will be relayed to the view
     * model. This function can be called whenever permission is withdrawn from the app and a failure occurs as a result.
     */
    private fun resolveLocationPermission() {
        Timber.d("Checking location permission workflow called...")
        // An inline function to actually perform a request for the permission.
        fun requestLocationPermission() {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
        // Check COARSE and FINE permissions granted.
        val checkAllPermissions: List<Int> = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ).map { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission)
        }
        when {
            // If we have permission granted to both COARSE and FINE location, set both as true in view model.
            checkAllPermissions[0] == PackageManager.PERMISSION_GRANTED && checkAllPermissions[1] == PackageManager.PERMISSION_GRANTED -> {
                Timber.d("We have permission granted for BOTH coarse and fine location!")
                worldMapViewModel.locationPermissionsUpdated(
                    coarseAccessGranted = true,
                    fineAccessGranted = true
                )
            }
            // If we have only coarse access granted, inform view model.
            checkAllPermissions[0] == PackageManager.PERMISSION_GRANTED -> {
                Timber.d("We have permission granted ONLY for coarse location.")
                worldMapViewModel.locationPermissionsUpdated(
                    coarseAccessGranted = true,
                    fineAccessGranted = false
                )
            }
            // Otherwise, determine if we require rationale shown for access to fine or coarse location, show a dialog.
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Timber.d("We must show request permission rationale for either COARSE or FINE location, so we will do that now.")
                // TODO: make some other dialog or thing outta this.
                val dialog = AlertDialog.Builder(requireContext())
                    .setMessage("We need location so you can join the world") //R.string.permission_rationale_location
                    .setPositiveButton(android.R.string.ok) { dialog, which -> // After click on Ok, request the permission.
                        requestLocationPermission()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create()
                dialog.show()
            }
            else -> {
                Timber.d("No need to show rationale, requesting location permission for both COARSE and FINE.")
                // Otherwise, request both COARSE and FINE location permission.
                requestLocationPermission()
            }
        }
    }

    /**
     * After location permission is granted and confirmed to be OK, this function will ensure location settings are currently configured to be compatible with what HawkSpeed
     * requires from the User. If they are not appropriate, the function will attempt to resolve these issues with the User's permission. The ultimate result will be relayed
     * to the view model.
     */
    private fun ensureLocationSettingsCompatible() {
        try {
            // Build a location settings request.
            val locationSettingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build()
            // Check location settings, and upon success update location settings status, on failure, attempt to solve the issue.
            settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener { locationSettingsResponse ->
                    Timber.d("Location settings have been determined to be compatible with HawkSpeed.")
                    worldMapViewModel.locationSettingsAppropriate(true)
                }
                .addOnFailureListener { exc ->
                    if (exc is ResolvableApiException) {
                        try {
                            // This is a resolvable API exception, use our resolutionForResult contract handler for this.
                            val intentSenderRequest = IntentSenderRequest.Builder(exc.resolution)
                                .build()
                            resolutionForResult.launch(intentSenderRequest)
                        } catch (sendExc: IntentSender.SendIntentException) {
                            throw sendExc
                        }
                    } else {
                        throw exc
                    }
                }
        } catch(e: Exception) {
            Timber.e("Failed to ensure location settings are appropriate!")
            worldMapViewModel.locationSettingsAppropriate(false)
            Timber.e(e)
        }
    }

    /**
     * A launcher for the contract from which permission to access the required location data is requested.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Get the outcome of granting permissions to FINE and COARSE location.
        val preciseGiven = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
        val coarseGiven = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        // Set in view model.
        worldMapViewModel.locationPermissionsUpdated(
            coarseAccessGranted = preciseGiven,
            fineAccessGranted = coarseGiven
        )
    }

    /**
     * A launcher for the contract from which location settings are resolved.
     */
    private val resolutionForResult = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if(activityResult.resultCode == Activity.RESULT_OK) {
            Timber.d("Successfully resolved location settings issues, ensuring settings are now appropriate.")
            // Now call ensure location settings compatible once more to double check.
            ensureLocationSettingsCompatible()
        } else {
            Timber.e("Failed to resolve invalid location settings, setting NOT APPROPRIATE.")
            worldMapViewModel.locationSettingsAppropriate(false)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment WorldMapFragment.
         */
        @JvmStatic
        fun newInstance() =
            WorldMapFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}