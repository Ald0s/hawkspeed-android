package com.vljx.hawkspeed.domain.interactor

interface BaseUseCase<in Parameter, out Result> {
    operator fun invoke(params: Parameter): Result
}