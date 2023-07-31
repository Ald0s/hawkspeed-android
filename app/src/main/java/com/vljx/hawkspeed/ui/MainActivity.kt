package com.vljx.hawkspeed.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.hardware.SensorManager
import android.os.Build
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.SettingsClient
import com.vljx.hawkspeed.WorldService
import com.vljx.hawkspeed.navigation.SetupNavGraph
import com.vljx.hawkspeed.ui.screens.authenticated.world.ActivityRecognitionPermissionCallback
import com.vljx.hawkspeed.ui.screens.authenticated.world.LocationPermissionSettingsCallback
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity(), MainWorldService, MainConfigurePermissions, MainCheckSensors {
    private var mIsServiceBound: Boolean = false
    private lateinit var mWorldService: WorldService

    private lateinit var settingsClient: SettingsClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var sensorManager: SensorManager

    private var locationPermissionSettingsCallback: LocationPermissionSettingsCallback? = null
    private var activityRecognitionPermissionCallback: ActivityRecognitionPermissionCallback? = null

    private val serviceConnection: ServiceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val worldServiceBinder = service as WorldService.WorldServiceBinder
            mWorldService = worldServiceBinder.getService()
            mIsServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mIsServiceBound = false
        }
    }

    override val isServiceBound: Boolean
        get() = mIsServiceBound

    override val worldService: WorldService
        get() = mWorldService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationRequest = WorldService.newLocationRequest()
        settingsClient = LocationServices.getSettingsClient(this)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        setContent {
            HawkspeedApp()
        }
    }

    override fun onStart() {
        super.onStart()
        bindService(
            Intent(this, WorldService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )
    }

    override fun onDestroy() {
        if(isFinishing) {
            // If activity is finishing, we can stop the service completely.
            if(mIsServiceBound) {
                // Service is bound, we will let the service know it must shut down.
                mWorldService.stopWorldService()
            } else {
                // No need to do any extra work, since its not found.
            }
        }
        // Unbind connection to the service.
        unbindService(serviceConnection)
        super.onDestroy()
    }

    override fun checkSensors(typesToCheck: List<Int>): Map<Int, MainCheckSensors.SensorReport> =
        typesToCheck.associateWith { typeId ->
            // TODO: we can get other info for each type here.
            MainCheckSensors.SensorReport(typeId, sensorManager.getDefaultSensor(typeId) != null)
        }

    override fun resolveLocationPermission(locationPermissionSettingsCallback: LocationPermissionSettingsCallback) {
        // Set the current permission settings callback, then begin a location permission resolution flow.
        this.locationPermissionSettingsCallback = locationPermissionSettingsCallback
        // Start permission resolution flow.
        resolveLocationPermission()
    }

    override fun resolveActivityRecognitionPermission(activityRecognitionPermissionCallback: ActivityRecognitionPermissionCallback) {
        // Set the current recog permission callback.
        this.activityRecognitionPermissionCallback = activityRecognitionPermissionCallback
        // Now, begin a resolution flow.
        resolveActivityRecognitionPermission()
    }

    /**
     * Check to ensure activity recognition permission has been granted, and attempt to resolve if not.
     */
    private fun resolveActivityRecognitionPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PERMISSION_GRANTED) {
                // Granted.
                activityRecognitionPermissionCallback?.activityRecognitionPermissionGranted()
            } else {
                // Request permissions.
                requestActivityTransitionUpdatesPermissionLauncher.launch(
                    Manifest.permission.ACTIVITY_RECOGNITION
                )
            }
        } else {
            // Immediately granted.
            activityRecognitionPermissionCallback?.activityRecognitionPermissionGranted()
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
            requestLocationPermissionLauncher.launch(
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
            ContextCompat.checkSelfPermission(this, permission)
        }
        when {
            // If we have permission granted to both COARSE and FINE location, set both as true in view model.
            checkAllPermissions[0] == PERMISSION_GRANTED && checkAllPermissions[1] == PERMISSION_GRANTED -> {
                Timber.d("We have permission granted for BOTH coarse and fine location!")
                locationPermissionSettingsCallback?.locationPermissionsUpdated(
                    coarseAccessGranted = true,
                    fineAccessGranted = true
                )
                // Call resolve settings, too.
                ensureLocationSettingsCompatible()
            }
            // If we have only coarse access granted, inform view model.
            checkAllPermissions[0] == PERMISSION_GRANTED -> {
                Timber.d("We have permission granted ONLY for coarse location.")
                locationPermissionSettingsCallback?.locationPermissionsUpdated(
                    coarseAccessGranted = true,
                    fineAccessGranted = false
                )
                // Call resolve settings, too.
                ensureLocationSettingsCompatible()
            }
            // Otherwise, determine if we require rationale shown for access to fine or coarse location, show a dialog.
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Timber.d("We must show request permission rationale for either COARSE or FINE location, so we will do that now.")
                // TODO: make some other dialog or thing outta this.
                val dialog = AlertDialog.Builder(this)
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
     * A launcher for the contract from which permission to access the required location data is requested.
     */
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Get the outcome of granting permissions to FINE and COARSE location.
        val preciseGiven = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
        val coarseGiven = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        // Set in view model.
        locationPermissionSettingsCallback?.locationPermissionsUpdated(
            coarseAccessGranted = preciseGiven,
            fineAccessGranted = coarseGiven
        )
    }

    /**
     * A launcher for the contract from which permission to access activity transition updates is requested.
     */
    private val requestActivityTransitionUpdatesPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        when(granted) {
            true -> activityRecognitionPermissionCallback?.activityRecognitionPermissionGranted()
            else -> activityRecognitionPermissionCallback?.activityRecognitionPermissionRefused()
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
                    locationPermissionSettingsCallback?.locationSettingsAppropriate(true)
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
            locationPermissionSettingsCallback?.locationSettingsAppropriate(false)
            Timber.e(e)
        }
    }

    /**
     * A launcher for the contract from which location settings are resolved.
     */
    private val resolutionForResult = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            Timber.d("Successfully resolved location settings issues, ensuring settings are now appropriate.")
            // Now call ensure location settings compatible once more to double check.
            ensureLocationSettingsCompatible()
        } else {
            Timber.e("Failed to resolve invalid location settings, setting NOT APPROPRIATE.")
            locationPermissionSettingsCallback?.locationSettingsAppropriate(false)
        }
    }
}

@Composable
fun HawkspeedApp() {
    // Remember a nav controller.
    val navHostController: NavHostController = rememberNavController()
    HawkSpeedTheme {
        // Now, setup the nav graph.
        SetupNavGraph(navHostController = navHostController)
    }
}