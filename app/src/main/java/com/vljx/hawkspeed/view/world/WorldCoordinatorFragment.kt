package com.vljx.hawkspeed.view.world

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.WorldService
import com.vljx.hawkspeed.databinding.FragmentWorldCoordinatorBinding
import com.vljx.hawkspeed.view.base.BaseFragment
import com.vljx.hawkspeed.viewmodel.world.WorldCoordinatorViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 * Use the [WorldCoordinatorFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class WorldCoordinatorFragment : BaseFragment<FragmentWorldCoordinatorBinding>() {
    private val worldCoordinatorViewModel: WorldCoordinatorViewModel by viewModels()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentWorldCoordinatorBinding
        get() = FragmentWorldCoordinatorBinding::inflate

    private lateinit var mWorldService: WorldService
    private var isServiceBound: Boolean = false

    // Access to the location client.
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    // Provides parameters for the location request.
    private lateinit var locationRequest: LocationRequest
    private lateinit var settingsClient: SettingsClient

    private val serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            val binder = service as WorldService.WorldServiceBinder
            mWorldService = binder.getService()
            isServiceBound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup location client.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = WorldService.newLocationRequest()
        settingsClient = LocationServices.getSettingsClient(requireActivity())

        arguments?.let {

        }
    }

    override fun onStart() {
        super.onStart()
        // Bind to the service.
        requireActivity().bindService(
            Intent(requireContext(), WorldService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return super.onCreateView(inflater, container, savedInstanceState).apply {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // By default, add a loading map fragment.
        childFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container_view, WorldLoadingFragment.newInstance(), "WORLD_LOADING_FRAGMENT_TAG")
        }
        // When view is created, we'll immediately check location permission prior to attaching flow collection.
        checkLocationPermission()
        // Setup our flow collections.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    worldCoordinatorViewModel.allDevicePermissionGiven.collectLatest { permissionGiven ->
                        Timber.d("All device permissions have been given! We will now check location settings compatibility and correct if required.")
                        ensureLocationSettingsCompatible()
                    }
                }

                launch {
                    worldCoordinatorViewModel.canLoadMapState.collectLatest { canLoadMapState ->
                        handleCanLoadMapStateChanged(canLoadMapState)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            LOCATION_SETTINGS_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK) {
                    Timber.d("Successfully resolved location settings issues.")
                    // Now call ensure location settings compatible once more to double check.
                    ensureLocationSettingsCompatible()
                } else {
                    Timber.e("Failed to resolve invalid location settings.")
                    worldCoordinatorViewModel.setLocationSettingsAppropriate(false)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStop() {
        // We'll unbind from the service on stop.
        requireActivity().unbindService(serviceConnection)
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun handleCanLoadMapStateChanged(canLoadMapState: CanLoadMapState) {
        when(canLoadMapState) {
            is CanLoadMapState.LoadAllowed -> {
                // Create a new world map fragment and replace it, UNLESS there is already one in there, in which case do not worry.
                if(childFragmentManager.findFragmentById(R.id.fragment_container_view) !is WorldMapFragment) {
                    Timber.d("Settings and configuration has been satisfied. We will now show the world map fragment...")
                    childFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace(R.id.fragment_container_view, WorldMapFragment.newInstance(), "WORLD_MAP_FRAGMENT_TAG")
                    }
                }
            }
            is CanLoadMapState.LoadDenied -> {
                Timber.d("Loading/joining the world has been DENIED! (set=${canLoadMapState.deviceSettingsAppropriate},prec=${canLoadMapState.preciseLocationPermissionGiven},coar=${canLoadMapState.coarseLocationPermissionGiven})")
            }
        }
    }

    private fun ensureLocationSettingsCompatible() {
        try {
            // Build a location settings request.
            val locationSettingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build()
            // Check location settings, and upon success update location settings status, on failure, attempt to solve the issue.
            settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener { locationSettingsResponse ->
                    Timber.d("Location settings successfully changed!")
                    worldCoordinatorViewModel.setLocationSettingsAppropriate(true)
                }
                .addOnFailureListener { exc ->
                    if (exc is ResolvableApiException) {
                        try {
                            exc.startResolutionForResult(requireActivity(),
                                LOCATION_SETTINGS_REQUEST_CODE
                            )
                        } catch (sendExc: IntentSender.SendIntentException) {
                            throw sendExc
                        }
                    } else {
                        throw exc
                    }
                }
        } catch(e: Exception) {
            worldCoordinatorViewModel.setLocationSettingsAppropriate(false)
            Timber.e(e)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val preciseGiven = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
        val coarseGiven = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)

        worldCoordinatorViewModel.setLocationPermissions(
            preciseLocationPermissionGiven = preciseGiven,
            coarseLocationPermissionGiven = coarseGiven
        )
    }

    private fun checkLocationPermission() {
        Timber.d("Checking location permission workflow called...")
        when {
            ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                Timber.d("We have checked location permission and have discovered we have already been granted precise location. Ensuring settings are compatible now.")
                worldCoordinatorViewModel.setLocationPermissions(
                    preciseLocationPermissionGiven = true,
                    coarseLocationPermissionGiven = true
                )
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Timber.d("Show rationale for access fine location")
                // TODO: make some other dialog or thing outta this.
                val dialog = AlertDialog.Builder(requireContext())
                    .setMessage("We need location so you can join the world") //R.string.permission_rationale_location
                    .setPositiveButton(android.R.string.ok) { dialog, which -> // After click on Ok, request the permission.
                        requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create()
                dialog.show()
            }
            else -> {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        }
    }

    companion object {
        const val LOCATION_SETTINGS_REQUEST_CODE = 0xafa
        const val LOCATION_PERMISSION_REQUEST_CODE = 0xafb

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment WorldCoordinatorFragment.
         */
        @JvmStatic
        fun newInstance() =
            WorldCoordinatorFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}