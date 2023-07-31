package com.vljx.hawkspeed.ui.screens.authenticated.setupsprinttrack

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.ui.component.InputValidationResult
import com.vljx.hawkspeed.ui.screens.common.LoadingScreen
import com.vljx.hawkspeed.ui.screens.common.TrackDraftPathOverview
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ExampleData
import com.vljx.hawkspeed.util.Extension.getActivity

@Composable
fun SetupSprintTrackDetailScreen(
    onTrackCreated: (TrackWithPath) -> Unit,

    setupSprintTrackDetailViewModel: SetupSprintTrackDetailViewModel = hiltViewModel()
) {
    val setupSprintTrackDetailUiState: SetupSprintTrackDetailUiState by setupSprintTrackDetailViewModel.setupSprintTrackDetailUiState.collectAsStateWithLifecycle()

    when(setupSprintTrackDetailUiState) {
        is SetupSprintTrackDetailUiState.SprintTrackCreated -> {
            LaunchedEffect(key1 = Unit, block = {
                onTrackCreated(
                    (setupSprintTrackDetailUiState as SetupSprintTrackDetailUiState.SprintTrackCreated).trackWithPath
                )
            })
        }
        is SetupSprintTrackDetailUiState.ShowSprintDetailForm -> {
            val trackNameState: String? by setupSprintTrackDetailViewModel.trackNameState.collectAsStateWithLifecycle()
            val trackDescriptionState: String? by setupSprintTrackDetailViewModel.trackDescriptionState.collectAsStateWithLifecycle()

            SetupSprintTrackDetail(
                showDetailForm = setupSprintTrackDetailUiState as SetupSprintTrackDetailUiState.ShowSprintDetailForm,

                trackName = trackNameState,
                trackDescription = trackDescriptionState,

                updateTrackName = setupSprintTrackDetailViewModel::updateTrackName,
                updateTrackDescription = setupSprintTrackDetailViewModel::updateTrackDescription,
                onSubmitTrackClicked = setupSprintTrackDetailViewModel::createTrack,

                componentActivity = LocalContext.current.getActivity()
            )
        }
        is SetupSprintTrackDetailUiState.Loading ->
            // The initial loading state, simply call loading screen composable.
            LoadingScreen()

        is SetupSprintTrackDetailUiState.LoadFailed -> {
            /**
             * TODO: load failed means the initial load process could not be completed. This is for things like, the indicated track draft id did not
             * TODO: correspond to an actual track draft. For this, we will require a separate UI composable.
             */
            throw NotImplementedError("Failed to setup track detail - Failed case is not handled.")
        }
    }
}

@Composable
fun SetupSprintTrackDetail(
    showDetailForm: SetupSprintTrackDetailUiState.ShowSprintDetailForm,

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

    LaunchedEffect(
        key1 = showDetailForm,
        block = {
            when(val setupTrackDetailFormUiState = showDetailForm.setupSprintTrackDetailFormUiState) {
                is SetupSprintTrackDetailFormUiState.SprintTrackDetailForm -> {
                    validateTrackNameResult = setupTrackDetailFormUiState.validateTrackName
                    validateTrackDescriptionResult = setupTrackDetailFormUiState.validateTrackDescription
                    canAttemptSubmitTrack = setupTrackDetailFormUiState.canAttemptSubmitTrack
                    isSubmitting = false
                }
                SetupSprintTrackDetailFormUiState.Submitting -> {
                    isSubmitting = true
                    setupTrackError = null
                }
                is SetupSprintTrackDetailFormUiState.ServerRefused -> {
                    setupTrackError = setupTrackDetailFormUiState.resourceError
                    isSubmitting = false
                }
            }
        }
    )

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
            SetupSprintTrackDetailForm(
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
fun SetupSprintTrackDetailForm(
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
                        enabled = canAttemptSubmitTrack,
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
        SetupSprintTrackDetailForm(
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
        SetupSprintTrackDetail(
            showDetailForm = SetupSprintTrackDetailUiState.ShowSprintDetailForm(
                trackDraftWithPoints = ExampleData.getTrackDraftWithPoints(),
                setupSprintTrackDetailFormUiState = SetupSprintTrackDetailFormUiState.SprintTrackDetailForm(
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