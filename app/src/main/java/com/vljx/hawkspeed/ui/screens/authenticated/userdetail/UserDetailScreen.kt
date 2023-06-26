package com.vljx.hawkspeed.ui.screens.authenticated.userdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun UserDetailScreen(
    userDetailViewModel: UserDetailViewModel = hiltViewModel()
) {
    val userDetailUi = userDetailViewModel.userDetailUiState.collectAsState()
}