package com.vljx.hawkspeed.ui.screens.authenticated.userdetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.ui.screens.common.LoadingScreen
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ExampleData

@Composable
fun UserDetailScreen(
    userDetailViewModel: UserDetailViewModel = hiltViewModel()
) {
    val userDetailUiState by userDetailViewModel.userDetailUiState.collectAsStateWithLifecycle()

    when(userDetailUiState) {
        is UserDetailUiState.GotUser ->
            UserDetail(user = (userDetailUiState as UserDetailUiState.GotUser).user)
        is UserDetailUiState.Loading ->
            LoadingScreen()
        is UserDetailUiState.Failed ->
            UserDetailLoadFailed((userDetailUiState as UserDetailUiState.Failed).resourceError)
    }
}

@Composable
fun UserDetail(
    user: User
) {
    // Remember a scroll state.
    val scrollState = rememberScrollState()
    // Set up a scaffold. For all the content; that is scrollable.
    Scaffold(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
    ) { paddingValues ->
        // Setup a box to take the scaffold's padding values.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
        ) {
            // Create a column to contain the separate sections.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // First, the user detail's head.
                UserDetailHead(user = user)
                Spacer(modifier = Modifier.height(24.dp))
                // Second, the user detail's achievement section.
                UserDetailAchievements(user = user)
                // TODO: tracks created?
                // TODO: vehicles?
            }
        }
    }
}

@Composable
fun UserDetailHead(
    user: User
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 48.dp, start = 32.dp, end = 32.dp)
    ) {
        // Set up a new row to contain the center aligned content.
        Row(
            modifier = Modifier
                .padding(bottom = 6.dp)
        ) {
            // The actual card for the profile image here.
            Card(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                ),
                modifier = Modifier
                    .height(92.dp)
                    .width(92.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // TODO: this is where we place the profile image.
                }
            }
        }
        // Set up a row here for the User's name.
        Row {
            Column(
                modifier = Modifier
            ) {
                // Now, the title of the track.
                Text(
                    text = user.userName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
        // Set up a row here for the User's bio. Only if the bio is not null.
        if(user.bio != null) {
            Column(
                modifier = Modifier
                    .padding(top = 12.dp)
            ) {
                Row {
                    Text(text = user.bio!!)
                }
            }
        }
    }
}

@Composable
fun UserDetailAchievements(
    user: User
) {

}

@Composable
fun UserDetailLoadFailed(
    resourceError: ResourceError
) {

}

@Preview
@Composable
fun PreviewUserDetail(

) {
    HawkSpeedTheme {
        UserDetail(
            user = ExampleData.getExampleUser()
        )
    }
}

@Preview
@Composable
fun PreviewUserDetailHead(

) {
    HawkSpeedTheme {
        Surface {
            UserDetailHead(user = ExampleData.getExampleUser())
        }
    }
}

@Preview
@Composable
fun PreviewUserDetailAchievements(

) {
    HawkSpeedTheme {
        Surface {
            UserDetailAchievements(user = ExampleData.getExampleUser())
        }
    }
}

@Preview
@Composable
fun PreviewUserDetailLoadFailed(

) {
    HawkSpeedTheme {
        UserDetailLoadFailed(resourceError = ResourceError.GeneralError("unknown", null))
    }
}