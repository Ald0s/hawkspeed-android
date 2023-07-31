package com.vljx.hawkspeed.ui.screens.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Polyline
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints

const val DEFAULT_RACE_TRACK_DRAFT_WIDTH = 25f

@Composable
fun DrawRaceTrackDraft(
    trackDraftWithPoints: TrackDraftWithPoints
) {
    Polyline(
        color = MaterialTheme.colorScheme.secondary,
        width = DEFAULT_RACE_TRACK_DRAFT_WIDTH,
        points = trackDraftWithPoints.pointDrafts.map { trackPoint ->
            LatLng(trackPoint.latitude, trackPoint.longitude)
        }
    )
}