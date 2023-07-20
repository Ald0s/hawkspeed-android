package com.vljx.hawkspeed.ui.screens.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.enums.TrackType
import com.vljx.hawkspeed.domain.models.track.Track

@Composable
fun TrackSubtitle(
    track: Track
) {
    // Within, the column, spawn a row that will contain two columns.
    Row(
        modifier = Modifier
            .padding(top = 4.dp)
    ) {
        // A column for the track type.
        Column {
            Text(
                text = stringResource(
                    id = when (track.trackType) {
                        TrackType.SPRINT -> R.string.track_type_sprint
                        TrackType.CIRCUIT -> R.string.track_type_circuit
                    }
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.titleMedium
            )
        }
        // A column for the separator bullet.
        Column(
            modifier = Modifier
                .padding(start = 6.dp, end = 6.dp)
        ) {
            Text(
                text = stringResource(id = R.string.bullet)
            )
        }
        // A column for extra type info. For sprints, this will be the total length. For circuits, this will be the number of laps.
        Column {
            Text(
                text = when(track.trackType) {
                    TrackType.SPRINT -> track.lengthFormatted
                    TrackType.CIRCUIT -> throw NotImplementedError("Circuit type is not implemented yet.")
                },
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}