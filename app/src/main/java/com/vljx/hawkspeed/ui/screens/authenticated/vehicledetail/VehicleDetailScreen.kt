package com.vljx.hawkspeed.ui.screens.authenticated.vehicledetail

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.vljx.hawkspeed.domain.models.user.User

@Composable
fun VehicleDetailScreen(
    onViewUserDetail: ((User) -> Unit)? = null,

    vehicleDetailViewModel: VehicleDetailViewModel = hiltViewModel()
) {
}