package com.vljx.hawkspeed.ui.screens.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vljx.hawkspeed.R
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetControls(
    desiredState: SheetValue,
    peekContent: @Composable (RowScope.() -> Unit),
    expandedContent: @Composable (RowScope.() -> Unit)? = null,
    sheetPeekHeight: Int = 128,
    peekContentPadding: PaddingValues = PaddingValues(top = 32.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    // Create a column to contain the minimum UI; which will be shown during the race.
    Column(
        modifier = Modifier
            .padding(peekContentPadding)
            .fillMaxWidth()
    ) {
        // A row within the column to contain the minimum UI.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetPeekHeight.dp)
        ) {
            peekContent()
        }
        expandedContent?.let {
            // A row to contain the rest of the content.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                it()
            }
        }
    }
    // Disposable side effect to expand this sheet fully but only for the duration of this state.
    BottomSheetTemporaryState(
        desiredState = desiredState,
        sheetState = scaffoldState.bottomSheetState,
        scope = scope
    )
}