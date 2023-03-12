package com.vljx.hawkspeed.viewmodel.world

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class WorldMapViewModel @Inject constructor(

): ViewModel() {
    private val mutableIsLoading: MutableStateFlow<Boolean> = MutableStateFlow(true)

    val isLoading: StateFlow<Boolean> =
        mutableIsLoading

    fun setLoading(loading: Boolean) {
        mutableIsLoading.tryEmit(loading)
    }
}