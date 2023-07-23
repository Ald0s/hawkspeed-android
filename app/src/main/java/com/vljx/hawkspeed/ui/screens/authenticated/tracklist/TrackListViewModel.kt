package com.vljx.hawkspeed.ui.screens.authenticated.tracklist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.vljx.hawkspeed.data.di.qualifier.IODispatcher
import com.vljx.hawkspeed.domain.usecase.user.PageUserTracksUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class TrackListViewModel @Inject constructor(
    private val pageUserTracksUseCase: PageUserTracksUseCase,

    private val savedStateHandle: SavedStateHandle,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher
): ViewModel() {
    /**
     * A mutable state flow for the UID belonging to the User we wish to view the tracks for.
     */
    private val mutableSelectedUserUid: MutableStateFlow<String> = MutableStateFlow(checkNotNull(savedStateHandle[ARG_USER_UID]))

    companion object {
        const val ARG_USER_UID = "userUid"
    }
}