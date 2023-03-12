package com.vljx.hawkspeed.presenter.setup

import com.vljx.hawkspeed.domain.requests.SetupProfileRequest

interface SetupProfilePresenter {
    fun setupProfileClicked(setupProfileRequest: SetupProfileRequest)
}