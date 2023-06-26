package com.vljx.hawkspeed.ui.screens.authenticated.trackdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TrackDetailScreen(
    trackDetailViewModel: TrackDetailViewModel = hiltViewModel()
) {
    val trackDetailUi by trackDetailViewModel.trackDetailUiState.collectAsState()
}