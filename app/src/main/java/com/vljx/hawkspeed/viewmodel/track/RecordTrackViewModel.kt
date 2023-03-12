package com.vljx.hawkspeed.viewmodel.track

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.interactor.track.SubmitTrackUseCase
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.requests.SubmitTrackPointRequest
import com.vljx.hawkspeed.domain.requests.SubmitTrackRequest
import com.vljx.hawkspeed.draft.track.RecordedPointDraft
import com.vljx.hawkspeed.draft.track.TrackDraft
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.xml.parsers.DocumentBuilderFactory

@HiltViewModel
class RecordTrackViewModel @Inject constructor(
    private val submitTrackUseCase: SubmitTrackUseCase
): ViewModel() {
    /**
     * A shared flow that will emit the new track's result, but will not replay values if user navigates away from view.
     */
    private val mutableNewTrackResult: MutableSharedFlow<Resource<Track>> = MutableSharedFlow(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )

    /**
     * The new track result.
     * This is received back as a track, that is, one without the full path data. Since this will be written to the map.
     */
    val newTrackResult: Flow<Resource<Track>> =
        mutableNewTrackResult.distinctUntilChanged()

    /**
     * The track's name.
     * TODO: two way binding to track name text view
     */
    val mutableTrackName: MutableStateFlow<String?> = MutableStateFlow(null)

    /**
     * The track's description.
     * TODO: two way binding to track description text view
     */
    val mutableTrackDescription: MutableStateFlow<String?> = MutableStateFlow(null)

    private val mutableIsRecording: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    private val mutableRecordedPoints: MutableStateFlow<List<RecordedPointDraft>?> =
        MutableStateFlow(listOf())
    private val mutablePointIndex: MutableStateFlow<Int> =
        MutableStateFlow(-1)

    /**
     * A state flow for whether the track name is valid.
     * For now, name is just required.
     */
    private val isNameValid: StateFlow<Boolean> =
        mutableTrackName.map { name ->
            name?.isBlank() == false
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * A state flow for whether the track description is valid.
     * For now, description is just required.
     */
    private val isDescriptionValid: StateFlow<Boolean> =
        mutableTrackDescription.map { desc ->
            desc?.isBlank() == false
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * A state flow for whether the points are valid.
     * They are valid when the list is not null, and there are at least 10 points.
     */
    private val arePointsValid: StateFlow<Boolean> =
        mutableRecordedPoints.map { recordedPointDrafts ->
            recordedPointDrafts != null && recordedPointDrafts.size >= 10
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * A state flow for whether this track's attributes are currently valid.
     * This is simply a combination of all other validation state flows.
     */
    private val isTrackValid: StateFlow<Boolean> =
        combine(
            isNameValid,
            isDescriptionValid,
            arePointsValid
        ) { nameValid, descValid, pointsValid ->
            nameValid && descValid && pointsValid
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * A state flow for the latest track draft.
     * When all required variables are at least not null, this will be rendered non-null, representing what will be sent to the foreign server.
     * TODO: use as parameter to presenter call on button click for submit track
     */
    val trackDraft: StateFlow<TrackDraft?> =
        combine(
            mutableTrackName,
            mutableTrackDescription,
            mutableRecordedPoints
        ) { name, description, points ->
            if(name != null && description != null && points != null) {
                return@combine TrackDraft(name, description, points)
            } else {
                return@combine null
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * A state flow for whether the track can be submitted.
     * It can be submitted when the track is determined valid, and the current track draft is not null.
     * TODO: one way binding on enabled attribute for button responsible for submitting track
     */
    val canSubmitTrack: StateFlow<Boolean> =
        combine(
            trackDraft,
            isTrackValid
        ) { track, isValid ->
            track != null && isValid
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * A state flow that will emit true when location points are currently being saved.
     */
    val isRecording: StateFlow<Boolean> =
        mutableIsRecording

    /**
     * A state flow that will emit true if at least one point is recorded, or the latest point index is greater than -1, its default value.
     */
    val hasRecordedHistory: StateFlow<Boolean> =
        combine(
            mutablePointIndex,
            mutableRecordedPoints
        ) { pointIndex, recordedPoints ->
            pointIndex > -1 || recordedPoints?.isNotEmpty() == true
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * A state flow that will emit the current list of recorded point drafts, in order of point index.
     */
    val recordedPoints: StateFlow<List<RecordedPointDraft>?> =
        mutableRecordedPoints

    /**
     * We can use the recorded track when we are not recording and we have history.
     */
    val canUseRecordedTrack: StateFlow<Boolean> =
        combine(
            isRecording,
            hasRecordedHistory
        ) { recording, hasHistory ->
            !recording && hasHistory
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * We can record if we are not recording, and we have no saved history.
     */
    val canRecord: StateFlow<Boolean> =
        combine(
            isRecording,
            hasRecordedHistory
        ) { recording, hasHistory ->
            !recording && !hasHistory
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * We can stop if we are recording.
     */
    val canStop: StateFlow<Boolean> =
        isRecording

    /**
     * We can only reset the track if we are not recording (which means we're in a stopped state/reset state,) and we have no recorded history;
     * meaning we are only in a reset state.
     */
    val canResetTrack: StateFlow<Boolean> =
        combine(
            isRecording,
            hasRecordedHistory
        ) { recording, hasHistory ->
            !recording && hasHistory
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * Start the recording of points.
     * This will clear points prior to recording.
     */
    fun record() {
        // Reset all values.
        reset()
        // Set is recording to true.
        mutableIsRecording.tryEmit(true)
    }

    /**
     * Stop the recording of points.
     */
    fun stop() {
        // Set is recording to false, do not reset values though.
        mutableIsRecording.tryEmit(false)
    }

    /**
     * Clear all recorded progress.
     */
    fun reset() {
        mutablePointIndex.tryEmit(-1)
        mutableRecordedPoints.tryEmit(null)
    }

    /**
     * The function that will, if recording is enabled, will create a recorded point draft from the location and add it to the list of points.
     */
    fun locationReceived(location: Location) {
        // If we are not recording, don't save the location.
        if(!isRecording.value) {
            return
        }
        // Otherwise, instantiate a new recorded point draft, and add it to the current list of recorded point drafts.
        // Get the latest point index, incremented by one, then set the latest index to that
        val pointIndex: Int = mutablePointIndex.value + 1
        mutablePointIndex.tryEmit(pointIndex)
        // Get the current recorded points list, as a mutable. If it is null, create a new one.
        val recordedPoints: MutableList<RecordedPointDraft> = mutableRecordedPoints.value?.toMutableList()
            ?: mutableListOf()
        // Add the latest point to it.
        recordedPoints.add(
            RecordedPointDraft(
                pointIndex,
                location
            )
        )
        // Set the latest recorded points list to the mutable.
        mutableRecordedPoints.tryEmit(recordedPoints)
    }

    /**
     * Submit this track to the server.
     * This will first convert the track draft into an XML document containing a GPX object.
     */
    fun submitTrack(trackDraft: TrackDraft) {
        if(!canSubmitTrack.value) {
            // TODO: properly handle this error case.
           //throw NotImplementedError("Failed to submit track draft as canSubmitTrack is false. This is also not handled.")
        }
        // Launch the rest in view model scope.
        viewModelScope.launch {
            // Now we can construct our new track request.
            val submitTrackRequest = SubmitTrackRequest(
                trackDraft.name,
                trackDraft.description,
                trackDraft.points.map { recordedPointDraft ->
                    SubmitTrackPointRequest(
                        recordedPointDraft.latitude,
                        recordedPointDraft.longitude,
                        recordedPointDraft.loggedAt,
                        recordedPointDraft.speed,
                        recordedPointDraft.rotation
                    )
                }
            )
            // Finally, we can utilise our track submission use case to complete the process. We will emit all from this invocation to our result shared flow.
            mutableNewTrackResult.emitAll(
                submitTrackUseCase(submitTrackRequest)
            )
        }
    }
}