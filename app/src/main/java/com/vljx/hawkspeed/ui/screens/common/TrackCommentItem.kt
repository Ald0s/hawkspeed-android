package com.vljx.hawkspeed.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.models.track.TrackComment
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ExampleData

@Composable
fun TrackCommentItem(
    trackComment: TrackComment,

    onEditCommentClicked: ((TrackComment) -> Unit)? = null,
    onDeleteCommentClicked: ((TrackComment) -> Unit)? = null
) {
    Surface(
        modifier = Modifier
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            // The track comment's User info bar, including menu for editing & deleting.
            TrackCommentUser(
                profileImage = null,
                user = trackComment.user,
                createdDurationDescription = trackComment.createdDurationDescription,

                onEditCommentClicked = onEditCommentClicked,
                onDeleteCommentClicked = onDeleteCommentClicked
            )
            // The track comment's body.
            TrackCommentBody(
                text = trackComment.text,
                modifier = Modifier
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun TrackCommentUser(
    profileImage: String?,
    user: User,
    createdDurationDescription: String,

    modifier: Modifier = Modifier,
    onEditCommentClicked: ((TrackComment) -> Unit)? = null,
    onDeleteCommentClicked: ((TrackComment) -> Unit)? = null
) {
    Row(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(end = 8.dp)
                .height(56.dp)
                .width(56.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .fillMaxSize()
            ) {
                // TODO: profile image here
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Row {
                Text(
                    text = user.userName,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Row(
                modifier = Modifier
            ) {
                Text(
                    text = createdDurationDescription,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(0.2f)
        ) {
            IconButton(
                onClick = {
                    // TODO: show the menu in which edit & delete can be accessed
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_more_horiz_24),
                    contentDescription = "more"
                )
            }
        }
    }
}

@Composable
fun TrackCommentBody(
    text: String,

    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
    ) {
        Column {
            Text(text = text)
        }
    }
}

@Preview
@Composable
fun PreviewTrackCommentItem(

) {
    HawkSpeedTheme {
        TrackCommentItem(
            trackComment = ExampleData.getExampleTrackComment()
        )
    }
}

@Preview
@Composable
fun PreviewTrackCommentUser(

) {
    // Pass a created timestamp 2 days, 2 hours and 35 minutes before the current time.
    val exampleTrackComment = ExampleData.getExampleTrackComment(
        createdSeconds =
        ((System.currentTimeMillis() / 1000L) - (172800 + 9300)).toInt()
    )

    HawkSpeedTheme {
        Surface {
            TrackCommentUser(
                profileImage = null,
                user = exampleTrackComment.user,
                createdDurationDescription = exampleTrackComment.createdDurationDescription
            )
        }
    }
}

@Preview
@Composable
fun PreviewTrackCommentBody(

) {
    val exampleTrackComment = ExampleData.getExampleTrackComment()

    HawkSpeedTheme {
        Surface {
            TrackCommentBody(
                text = exampleTrackComment.text
            )
        }
    }
}