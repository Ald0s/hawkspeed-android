package com.vljx.hawkspeed.viewmodel.track

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.interactor.track.GetTrackPathUseCase
import com.vljx.hawkspeed.domain.interactor.track.GetTrackUseCase
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.domain.models.track.TrackPoint
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.domain.requests.track.GetTrackPathRequest
import com.vljx.hawkspeed.domain.requests.track.GetTrackRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import java.lang.Thread.State
import javax.inject.Inject

@HiltViewModel
class TrackDetailViewModel @Inject constructor(
    private val getTrackUseCase: GetTrackUseCase,
    private val getTrackPathUseCase: GetTrackPathUseCase
): ViewModel() {
    /**
     * A mutable shared flow to contain currently selected track's UID, that starts empty, but will also replay the currently selected value when
     * view is navigated to/from.
     */
    private val mutableSelectedTrackUid: MutableSharedFlow<String> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Maps the UID to a resource for the desired track.
     */
    val selectedTrackResource: Flow<Resource<Track>> =
        mutableSelectedTrackUid.flatMapLatest { trackUid ->
            getTrackUseCase(
                GetTrackRequest(trackUid)
            )
        }

    /**
     * Maps the UID to a resource for the desired track's full path.
     */
    val selectedTrackPathResource: Flow<Resource<TrackPath>> =
        mutableSelectedTrackUid.flatMapLatest { trackUid ->
            getTrackPathUseCase(
                GetTrackPathRequest(trackUid)
            )
        }

    /**
     * The following flows all relate to a successful emission of the selected track resource.
     */
    private val selectedTrack: Flow<Track> =
        selectedTrackResource.transformLatest { trackResource ->
            if (trackResource.status == Resource.Status.SUCCESS) {
                emit(trackResource.data!!)
            }
        }

    /**
     * The track's name.
     */
    val name: StateFlow<String?> =
        selectedTrack.map { track ->
            track.name
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * The track's description.
     */
    val description: StateFlow<String?> =
        selectedTrack.map { track ->
            track.description
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * The track's owner.
     */
    val owner: StateFlow<User?> =
        selectedTrack.map { track ->
            track.owner
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * The track's start point.
     */
    val startPoint: StateFlow<TrackPoint?> =
        selectedTrack.map { track ->
            track.startPoint
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Whether the track is verified or not.
     */
    val isVerified: StateFlow<Boolean?> =
        selectedTrack.map { track ->
            track.isVerified
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Whether the track can be raced.
     */
    val canRace: StateFlow<Boolean?> =
        selectedTrack.map { track ->
            track.canRace
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Whether the track can be edited.
     */
    val canEdit: StateFlow<Boolean?> =
        selectedTrack.map { track ->
            track.canEdit
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Whether the track can be deleted.
     */
    val canDelete: StateFlow<Boolean?> =
        selectedTrack.map { track ->
            track.canDelete
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // TODO: leaderboard
    // TODO: ratings
    // TODO: comments

    /**
     * Select the track with the given UID.
     */
    fun selectTrack(trackUid: String) {
        mutableSelectedTrackUid.tryEmit(trackUid)
    }
}