package com.vljx.hawkspeed.domain.base

interface BasePaged {
    val thisPage: Int
    val nextPage: Int?
}