package com.vljx.hawkspeed.ui.dialogs.trackpreview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackPoint
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme

@Composable
fun TrackPreviewDialog(
    trackUid: String,
    onViewTrackClicked: (Track) -> Unit,
    onDismiss: () -> Unit,
    trackPreviewViewModel: TrackPreviewViewModel = hiltViewModel()
) {
    // Immediately select the given track in our view model.
    trackPreviewViewModel.selectTrack(trackUid)

    // Collect the track preview here.
    val trackPreviewUiState by trackPreviewViewModel.trackPreviewUiState.collectAsState()

    when(trackPreviewUiState) {
        is TrackPreviewUiState.GotTrack -> {
            val track = (trackPreviewUiState as TrackPreviewUiState.GotTrack).track

            Dialog(
                onDismissRequest = onDismiss
            ) {
                TrackPreviewCard(
                    track = track,
                    onViewTrackClicked = onViewTrackClicked
                )
            }
        }

        is TrackPreviewUiState.Loading -> {
            // TODO: some loading indication.
        }

        is TrackPreviewUiState.Failed -> {
            // TODO: some failed indication.
        }
    }
}

@Composable
fun TrackPreviewCard(
    track: Track,
    onViewTrackClicked: (Track) -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {

        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = track.name,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column {

                }
            }
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = {

                    }
                ) {
                    Text(text = "CANCEL")
                }
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(
                    onClick = {

                    }
                ) {
                    Text(text = "OK")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTrackPreviewCard() {
    HawkSpeedTheme {
        TrackPreviewCard(
            track = Track(
                "TRACK01"
                , "Yarra Boulevard",
                "A nice race.",
                User("USER01", "aldos", 0, false, true),
                TrackPoint(0.0,0.0,"TRACK01"),
                true,
                5,
                1,
                null, 3,
                true,
                true,
                true
            ),
            onViewTrackClicked = { track ->

            }
        )
    }
}