package com.vljx.hawkspeed.draft.track

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RecordedTrackDraft(
    val recordedPoints: List<RecordedPointDraft>
): Parcelable