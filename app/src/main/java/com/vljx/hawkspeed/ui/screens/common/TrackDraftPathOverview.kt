package com.vljx.hawkspeed.ui.screens.common

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.vljx.hawkspeed.Extension.toOverviewCameraUpdate
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.models.world.BoundingBox
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ExampleData

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
    // Remember mutable state for when map is loaded.
    var isMapLoaded by remember { mutableStateOf<Boolean>(false) }
    // The track draft path overview's camera position state, set to first point in the track as default...
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(trackDraftWithPoints.firstPointDraft!!.latitude, trackDraftWithPoints.firstPointDraft!!.longitude),
            15f
        )
    }

    LaunchedEffect(key1 = isMapLoaded, block = {
        if(isMapLoaded) {
            // When map is loaded, cause a change to the camera such that we move to the draft track path's bounds.
            val boundingBox: BoundingBox = trackDraftWithPoints.getBoundingBox()
            cameraPositionState.move(boundingBox.toOverviewCameraUpdate())
        } else {
            // TODO: animated visibility ???
        }
    })

    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
            compassEnabled = false,
            myLocationButtonEnabled = false,
            indoorLevelPickerEnabled = false,
            mapToolbarEnabled = false,
            rotationGesturesEnabled = false,
            scrollGesturesEnabled = false,
            tiltGesturesEnabled = false,
            zoomGesturesEnabled = false,
            zoomControlsEnabled = false
        )
        )
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
            // We will draw a Google Map composable, with position locked on the track's path above. And some padding, too.
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = uiSettings,
                onMapLoaded = {
                    // Set map loaded.
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

@Preview
@Composable
fun PreviewTrackDraftPathOverview(

) {
    HawkSpeedTheme {
        Scaffold { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .wrapContentSize()
            ) {
                // Set up the track draft path overview here. We want to provide a modifier that will size the box appropriately.
                TrackDraftPathOverview(
                    trackDraftWithPoints = ExampleData.getTrackDraftWithPoints()
                )
            }
        }
    }
}