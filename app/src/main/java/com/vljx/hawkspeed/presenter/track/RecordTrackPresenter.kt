package com.vljx.hawkspeed.presenter.track

import com.vljx.hawkspeed.draft.track.RecordedPointDraft

interface RecordTrackPresenter {
    fun useRecordedTrackClicked(recordedPoints: List<RecordedPointDraft>)
    fun recordClicked()
    fun stopClicked()
    fun resetClicked()
}