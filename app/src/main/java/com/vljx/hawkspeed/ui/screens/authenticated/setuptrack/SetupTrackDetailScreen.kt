package com.vljx.hawkspeed.ui.screens.authenticated.setuptrack

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.vljx.hawkspeed.Extension.toOverviewCameraUpdate
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.models.world.BoundingBox
import com.vljx.hawkspeed.ui.component.InputValidationResult
import com.vljx.hawkspeed.ui.screens.authenticated.setup.UsernameAvailability
import com.vljx.hawkspeed.ui.screens.common.DrawRaceTrackDraft
import com.vljx.hawkspeed.ui.screens.common.Loading
import com.vljx.hawkspeed.ui.screens.common.LoadingScreen
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ExampleData
import com.vljx.hawkspeed.util.Extension.getActivity

/**
 * TODO: split the detail screen into two subscreens depending on the track type; sprint or circuit.
 * if sprint, we really have no extra options to set. If a circuit, we must select the number of laps
 */
@Composable
fun SetupTrackDetailScreen(
    onTrackCreated: (TrackWithPath) -> Unit,

    setupTrackDetailViewModel: SetupTrackDetailViewModel = hiltViewModel()
) {
    val setupTrackDetailUiState: SetupTrackDetailUiState by setupTrackDetailViewModel.setupTrackDetailUiState.collectAsState()

    when(setupTrackDetailUiState) {
        is SetupTrackDetailUiState.TrackCreated -> {
            LaunchedEffect(key1 = Unit, block = {
                onTrackCreated(
                    (setupTrackDetailUiState as SetupTrackDetailUiState.TrackCreated).trackWithPath
                )
            })
        }
        is SetupTrackDetailUiState.ShowDetailForm -> {
            val trackNameState: String? by setupTrackDetailViewModel.trackNameState.collectAsState()
            val trackDescriptionState: String? by setupTrackDetailViewModel.trackDescriptionState.collectAsState()

            SetupTrackDetail(
                showDetailForm = setupTrackDetailUiState as SetupTrackDetailUiState.ShowDetailForm,

                trackName = trackNameState,
                trackDescription = trackDescriptionState,

                updateTrackName = setupTrackDetailViewModel::updateTrackName,
                updateTrackDescription = setupTrackDetailViewModel::updateTrackDescription,
                onSubmitTrackClicked = setupTrackDetailViewModel::createTrack,

                componentActivity = LocalContext.current.getActivity()
            )
        }
        is SetupTrackDetailUiState.Loading -> {
            // The initial loading state, simply call loading screen composable.
            LoadingScreen()
        }
        is SetupTrackDetailUiState.LoadFailed -> {
            /**
             * TODO: load failed means the initial load process could not be completed. This is for things like, the indicated track draft id did not
             * TODO: correspond to an actual track draft. For this, we will require a separate UI composable.
             */
            throw NotImplementedError("Failed to setup track detail - Failed case is not handled.")
        }
    }
}

@Composable
fun SetupTrackDetail(
    showDetailForm: SetupTrackDetailUiState.ShowDetailForm,

    trackName: String?,
    trackDescription: String?,

    updateTrackName: ((String) -> Unit)? = null,
    updateTrackDescription: ((String) -> Unit)? = null,
    onSubmitTrackClicked: (() -> Unit)? = null,

    componentActivity: ComponentActivity? = null
) {
    var validateTrackNameResult: InputValidationResult by remember { mutableStateOf(InputValidationResult(false)) }
    var validateTrackDescriptionResult: InputValidationResult by remember { mutableStateOf(InputValidationResult(false)) }
    var canAttemptSubmitTrack: Boolean by remember { mutableStateOf(false) }
    var setupTrackError: ResourceError? by remember { mutableStateOf(null) }
    var isSubmitting: Boolean by remember { mutableStateOf(false) }
    when(val setupTrackDetailFormUiState = showDetailForm.setupTrackDetailFormUiState) {
        is SetupTrackDetailFormUiState.TrackDetailForm -> {
            validateTrackNameResult = setupTrackDetailFormUiState.validateTrackName
            validateTrackDescriptionResult = setupTrackDetailFormUiState.validateTrackDescription
            canAttemptSubmitTrack = setupTrackDetailFormUiState.canAttemptSubmitTrack
            isSubmitting = false
        }
        SetupTrackDetailFormUiState.Submitting -> {
            isSubmitting = true
            setupTrackError = null
        }
        is SetupTrackDetailFormUiState.ServerRefused -> {
            setupTrackError = setupTrackDetailFormUiState.resourceError
            isSubmitting = false
        }
    }

    // Set up a scaffold. For all the content.
    Scaffold(
        modifier = Modifier
    ) { paddingValues ->
        // Setup a new column to take from the scaffold padding.
        Column(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            // Set up a new row to contain the center aligned content.
            Row(
                modifier = Modifier
                    .padding(top = 32.dp, bottom = 16.dp)
            ) {
                // Setup a new column to hold the centered information; that is, the actual track path overview and the
                // track title and creator info.
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Set up the track draft path overview here. We want to provide a modifier that will size the box appropriately.
                    TrackDraftPathOverview(
                        showDetailForm.trackDraftWithPoints,
                        componentActivity = componentActivity
                    )
                }
            }
            // Set up a row here for the remainder of the UI- the form.
            SetupTrackDetailForm(
                trackName = trackName,
                validateTrackNameResult = validateTrackNameResult,
                trackDescription = trackDescription,
                validateTrackDescriptionResult = validateTrackDescriptionResult,
                canAttemptSubmitTrack = canAttemptSubmitTrack,
                isSubmitting = isSubmitting,

                updateTrackName = updateTrackName,
                updateTrackDescription = updateTrackDescription,
                onSubmitTrackClicked = onSubmitTrackClicked
            )
        }
    }
}

@Composable
fun TrackDraftPathOverview(
    trackDraftWithPoints: TrackDraftWithPoints,
    modifier: Modifier = Modifier,
    componentActivity: ComponentActivity? = null
) {
    // If there are 0 points (has recorded track returns false), this will throw an illegal state exc.
    if(!trackDraftWithPoints.hasRecordedTrack) {
        throw IllegalStateException()
    }
    // The track draft path overview's camera position state, set to first point in the track as default...
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(trackDraftWithPoints.firstPointDraft!!.latitude, trackDraftWithPoints.firstPointDraft!!.longitude),
            15f
        )
    }
    var isMapLoaded by remember { mutableStateOf<Boolean>(false) }

    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        modifier = modifier
            .height(300.dp)
            .fillMaxWidth(0.6f)
            .padding(bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            var uiSettings by remember {
                mutableStateOf(MapUiSettings(
                    compassEnabled = false,
                    myLocationButtonEnabled = false,
                    indoorLevelPickerEnabled = false,
                    mapToolbarEnabled = false,
                    rotationGesturesEnabled = false,
                    scrollGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                    zoomGesturesEnabled = false,
                    zoomControlsEnabled = false
                ))
            }
            var mapProperties by remember {
                mutableStateOf(MapProperties(
                    isBuildingEnabled = false,
                    isIndoorEnabled = false,
                    isMyLocationEnabled = false,
                    minZoomPreference = 3.0f,
                    maxZoomPreference = 21.0f,
                    mapStyleOptions = componentActivity?.let { activity ->
                        MapStyleOptions.loadRawResourceStyle(
                            activity,
                            R.raw.worldstyle
                        )
                    }
                ))
            }
            // We will draw a Google Map composable, with position locked on the track's path above. And some padding, too.
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = uiSettings,
                onMapLoaded = {
                    // When map is loaded, cause a change to the camera such that we move to the draft track path's bounds.
                    val boundingBox: BoundingBox = trackDraftWithPoints.getBoundingBox()
                    cameraPositionState.move(boundingBox.toOverviewCameraUpdate())
                    isMapLoaded = true
                }
            ) {
                // Draw the race track.
                DrawRaceTrackDraft(
                    trackDraftWithPoints = trackDraftWithPoints
                )
            }
            // If map is not yet loaded, overlay an animated visibility over the top.
            if(!isMapLoaded) {
                AnimatedVisibility(
                    modifier = Modifier
                        .fillMaxSize(),
                    visible = true,
                    enter = EnterTransition.None,
                    exit = fadeOut()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .wrapContentSize()
                    )
                }
            }
        }
    }
}

@Composable
fun SetupTrackDetailForm(
    trackName: String?,
    validateTrackNameResult: InputValidationResult,
    trackDescription: String?,
    validateTrackDescriptionResult: InputValidationResult,
    canAttemptSubmitTrack: Boolean,
    isSubmitting: Boolean,

    updateTrackName: ((String) -> Unit)? = null,
    updateTrackDescription: ((String) -> Unit)? = null,
    onSubmitTrackClicked: (() -> Unit)? = null
) {
    Surface {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.8f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            enabled = !isSubmitting,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "track name"
                                )
                            },
                            value = trackName ?: "",
                            placeholder = {
                                Text(text = stringResource(id = R.string.placeholder_setup_track_name))
                            },
                            onValueChange = updateTrackName ?: { },
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    enabled = !isSubmitting,
                    singleLine = false,
                    maxLines = 3,
                    value = trackDescription ?: "",
                    onValueChange = updateTrackDescription ?: { },
                    placeholder = {
                        Text(text = stringResource(id = R.string.placeholder_setup_track_description))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                if(!isSubmitting) {
                    Button(
                        onClick = onSubmitTrackClicked ?: { },
                        enabled = canAttemptSubmitTrack && !isSubmitting,
                        shape = RectangleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.setup_track_finish).uppercase())
                    }
                } else {
                    // TODO: loading indicator here.
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewSetupTrackDetailForm(

) {
    HawkSpeedTheme {
        SetupTrackDetailForm(
            "yarra b",
            InputValidationResult(true),
            "this is a cool track!",
            InputValidationResult(true),
            canAttemptSubmitTrack = true,
            isSubmitting = false
        )
    }
}

@Preview
@Composable
fun PreviewSetupTrackDetail(

) {
    HawkSpeedTheme {
        SetupTrackDetail(
            showDetailForm = SetupTrackDetailUiState.ShowDetailForm(
                trackDraftWithPoints = ExampleData.getTrackDraftWithPoints(),
                setupTrackDetailFormUiState = SetupTrackDetailFormUiState.TrackDetailForm(
                    InputValidationResult(true),
                    InputValidationResult(true),
                    true
                )
            ),
            "yarra b",
            "This is a cool track"
        )
    }
}