package com.vljx.hawkspeed.ui.screens.common

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Opens a disposable side effect that will animate the given sheet state to the desired state, but only until the composition is disposed,
 * at which point, the value set upon entry will be used.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetTemporaryState(
    desiredState: SheetValue,
    sheetState: SheetState,
    scope: CoroutineScope
) {
    DisposableEffect(key1 = Unit, effect = {
        // When we enter composition, we will save the current state of the sheet state.
        val oldState = sheetState.currentValue
        scope.launch {
            // Now, adjust to the desired state.
            when(desiredState) {
                SheetValue.Hidden -> sheetState.hide()
                SheetValue.PartiallyExpanded -> sheetState.partialExpand()
                SheetValue.Expanded -> sheetState.expand()
            }
        }
        // Now, add a dispose that will, when triggered, return the sheet to its old state.
        onDispose {
            scope.launch {
                when(oldState) {
                    SheetValue.Hidden -> sheetState.hide()
                    SheetValue.PartiallyExpanded -> sheetState.partialExpand()
                    SheetValue.Expanded -> sheetState.expand()
                }
            }
        }
    })
}