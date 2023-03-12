package com.vljx.hawkspeed.presenter.onboard

import com.vljx.hawkspeed.domain.requests.LoginRequest

interface LoginPresenter {
    fun loginClicked(loginRequest: LoginRequest)
}