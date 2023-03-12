package com.vljx.hawkspeed.data.mapper

interface Mapper<Model, Domain> {
    fun mapFromData(model: Model): Domain
    fun mapToData(domain: Domain): Model
}