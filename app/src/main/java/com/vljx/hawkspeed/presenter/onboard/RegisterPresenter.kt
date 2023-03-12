package com.vljx.hawkspeed.presenter.onboard

import com.vljx.hawkspeed.domain.requests.RegisterLocalAccountRequest

interface RegisterPresenter {
    fun registerClicked(registrationRequest: RegisterLocalAccountRequest)
}