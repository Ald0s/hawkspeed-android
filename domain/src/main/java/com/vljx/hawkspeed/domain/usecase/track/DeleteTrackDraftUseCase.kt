package com.vljx.hawkspeed.domain.usecase.track

import com.vljx.hawkspeed.domain.usecase.BaseSuspendingUseCase
import javax.inject.Inject

class DeleteTrackDraftUseCase @Inject constructor(

): BaseSuspendingUseCase<Long, Unit> {
    override suspend fun invoke(params: Long) {
        TODO("Not yet implemented")
    }
}