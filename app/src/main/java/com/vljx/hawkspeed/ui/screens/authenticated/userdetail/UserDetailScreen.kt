package com.vljx.hawkspeed.ui.screens.authenticated.userdetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.ui.screens.common.LoadingScreen
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ExampleData

@Composable
fun UserDetailScreen(
    userDetailViewModel: UserDetailViewModel = hiltViewModel()
) {
    val userDetailUiState by userDetailViewModel.userDetailUiState.collectAsState()

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
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                ) {
                    // TODO: this is where we must place a profile image.
                }
            }
        }
    }
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
fun PreviewuserDetailLoadFailed(

) {
    HawkSpeedTheme {
        UserDetailLoadFailed(resourceError = ResourceError.GeneralError("unknown", null))
    }
}