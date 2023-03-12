package com.vljx.hawkspeed.domain.interactor

interface BaseSuspendingUseCase<in Parameter, out Result> {
    suspend operator fun invoke(params: Parameter): Result
}