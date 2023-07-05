package com.vljx.hawkspeed.ui.screens.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.util.ThirdParty

@Composable
fun DrawRaceTrack(
    track: Track,
    trackPath: TrackPath?,
    onTrackMarkerClicked: ((Marker, Track) -> Unit)? = null
) {
    Marker(
        state = MarkerState(
            position = LatLng(
                track.startPoint.latitude,
                track.startPoint.longitude
            )
        ),
        title = track.name,
        snippet = track.description,
        icon = ThirdParty.vectorToBitmap(
            LocalContext.current,
            R.drawable.ic_route,
            MaterialTheme.colorScheme.primary
        ),
        onClick = { trackMarker ->
            onTrackMarkerClicked?.invoke(trackMarker, track)
            true
        }
    )

    trackPath?.let { path ->
        // Create the Track's path.
        Polyline(
            color = MaterialTheme.colorScheme.secondary,
            points = path.points.map { trackPoint ->
                LatLng(trackPoint.latitude, trackPoint.longitude)
            }
        )
    }
}

/*@Composable
fun DrawRaceTrack(
    points: List<LatLng>
) {
    Polyline(
        color = MaterialTheme.colorScheme.secondary,
        points = points
    )
}*/