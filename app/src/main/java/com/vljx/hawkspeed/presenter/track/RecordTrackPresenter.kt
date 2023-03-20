package com.vljx.hawkspeed.presenter.track

import com.vljx.hawkspeed.draft.track.RecordedPointDraft
import com.vljx.hawkspeed.draft.track.TrackDraft

interface RecordTrackPresenter {
    /**
     * The following functions are for recording the track.
     */
    fun useRecordedTrackClicked(recordedPoints: List<RecordedPointDraft>)
    fun recordTrackClicked()
    fun stopRecordingClicked()
    fun resetRecordingClicked()

    /**
     * The following functions are for providing final details for the track.
     */
    fun createNewTrack(trackDraft: TrackDraft?)
    fun backToRecordingTrack()
}