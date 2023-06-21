package com.vljx.hawkspeed.domain.usecase

interface BaseUseCase<in Parameter, out Result> {
    operator fun invoke(params: Parameter): Result
}