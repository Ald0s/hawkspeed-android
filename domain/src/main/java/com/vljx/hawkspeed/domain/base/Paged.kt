package com.vljx.hawkspeed.domain.base

interface Paged {
    val thisPage: Int
    val nextPage: Int?
}