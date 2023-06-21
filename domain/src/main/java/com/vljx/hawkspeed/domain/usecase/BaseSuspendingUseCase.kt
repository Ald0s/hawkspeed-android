package com.vljx.hawkspeed.domain.usecase

interface BaseSuspendingUseCase<in Parameter, out Result> {
    suspend operator fun invoke(params: Parameter): Result
}