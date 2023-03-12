package com.vljx.hawkspeed.domain.di.component

import com.vljx.hawkspeed.domain.di.scope.AuthenticationScope
import dagger.hilt.DefineComponent
import dagger.hilt.components.SingletonComponent

@AuthenticationScope
@DefineComponent(parent = SingletonComponent::class)
interface AuthenticationComponent {
}