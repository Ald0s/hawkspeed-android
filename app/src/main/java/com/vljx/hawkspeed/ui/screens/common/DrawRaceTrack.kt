package com.vljx.hawkspeed.ui.screens.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberMarkerState
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.util.ThirdParty

const val DEFAULT_RACE_TRACK_WIDTH = 25f

sealed class RaceTrackDisplayMode {
    /**
     * A display mode that should set the race track's marker visible and if one is given, draw its track path.
     */
    object Full: RaceTrackDisplayMode()

    /**
     * A display mode that should hide the track, even if its given, but display the marker.
     */
    object Partial: RaceTrackDisplayMode()

    /**
     * A display mode that will not show the marker or the track, but some indication that an object is there.
     */
    object None: RaceTrackDisplayMode()
}

@Composable
fun DrawRaceTrack(
    track: Track,
    trackPath: TrackPath?,
    displayMode: RaceTrackDisplayMode = RaceTrackDisplayMode.Full,
    onTrackMarkerClicked: ((Track) -> Unit)? = null
) {
    val markerState = rememberMarkerState(
        position = LatLng(
            track.startPoint.latitude,
            track.startPoint.longitude
        )
    )
    var trackPathLatLng by remember {
        mutableStateOf<List<LatLng>>(listOf())
    }

    LaunchedEffect(
        key1 = trackPath?.hash,
        block = {
            trackPath?.points?.let { trackPoints ->
                trackPathLatLng = trackPoints.map { trackPoint ->
                    LatLng(trackPoint.latitude, trackPoint.longitude)
                }
            }
        }
    )

    // If display mode full or partial, draw the marker.
    if(displayMode is RaceTrackDisplayMode.Full || displayMode is RaceTrackDisplayMode.Partial) {
        Marker(
            state = markerState,
            title = track.name,
            snippet = track.description,
            icon = ThirdParty.vectorToBitmap(
                LocalContext.current,
                R.drawable.ic_route,
                MaterialTheme.colorScheme.primary
            ),
            onClick = { trackMarker ->
                onTrackMarkerClicked?.invoke(track)
                true
            }
        )
    } else {
        // If display mode is None, we'll draw an indication.
        /**
         * TODO: draw an indication that the track is here. Perhaps like a single dot or something inexpensive.
         */
    }

    // If display mode full, draw the polyline.
    if(displayMode is RaceTrackDisplayMode.Full) {
        Polyline(
            color = MaterialTheme.colorScheme.secondary,
            width = DEFAULT_RACE_TRACK_WIDTH,
            points = trackPathLatLng
        )
    }
}