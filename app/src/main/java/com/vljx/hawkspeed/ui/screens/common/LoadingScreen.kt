package com.vljx.hawkspeed.ui.screens.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingScreen(
    @StringRes loadingResId: Int = R.string.loading
) {
    Scaffold { paddingValues ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(paddingValues)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(72.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    style = MaterialTheme.typography.headlineMedium,
                    text = stringResource(id = R.string.loading)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoadingScreen(

) {
    HawkSpeedTheme {
        LoadingScreen()
    }
}