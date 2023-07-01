package com.vljx.hawkspeed.ui.screens.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Polyline

@Composable
fun DrawRaceTrack(
    points: List<LatLng>
) {
    Polyline(
        color = MaterialTheme.colorScheme.secondary,
        points = points
    )
}