package com.vljx.hawkspeed.ui.screens.authenticated.setuptrack

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.vljx.hawkspeed.domain.models.track.TrackWithPath

@Composable
fun SetupTrackDetailScreen(
    onTrackCreated: (TrackWithPath) -> Unit,
    setupTrackDetailViewModel: SetupTrackDetailViewModel = hiltViewModel()
) {
    val setupTrackDetailUiState: SetupTrackDetailUiState by setupTrackDetailViewModel.setupTrackDetailUiState.collectAsState(
        initial = SetupTrackDetailUiState.Idle
    )
    val canSubmitTrack: Boolean by setupTrackDetailViewModel.canAttemptCreateTrack.collectAsState()

    when(setupTrackDetailUiState) {
        is SetupTrackDetailUiState.TrackCreated -> {
            // TODO: track was successfully created, launch a side effect to invoke callback.
            LaunchedEffect(key1 = Unit, block = {
                onTrackCreated(
                    (setupTrackDetailUiState as SetupTrackDetailUiState.TrackCreated).trackWithPath
                )
            })
        }
        is SetupTrackDetailUiState.Failed -> {
            // TODO: handle error while setting track up.
            throw NotImplementedError("Failed to setup track detail - Failed case is not handled.")
        }
        is SetupTrackDetailUiState.Loading -> {
            // TODO: some loading indicator, hide/set disabled submit button.
        }
        is SetupTrackDetailUiState.Idle -> {
            // Idle does nothing for now.
        }
    }
    // TODO: create a form composable for this screen.
}