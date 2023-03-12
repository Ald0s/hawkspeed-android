package com.vljx.hawkspeed.domain.di.component

import dagger.hilt.DefineComponent

@DefineComponent.Builder
interface AuthenticationComponentBuilder {
    // TODO: setAuthenticationSeed(@BindsInstance data): AuthenticationComponentBuilder
    fun build(): AuthenticationComponent
}