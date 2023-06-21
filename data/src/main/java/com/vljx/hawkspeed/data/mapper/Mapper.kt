package com.vljx.hawkspeed.data.mapper

interface Mapper<Model, Domain> {
    fun mapFromData(model: Model): Domain
    fun mapFromDataList(models: List<Model>): List<Domain> =
        models.map { model -> mapFromData(model) }

    fun mapToData(domain: Domain): Model
    fun mapToDataList(domains: List<Domain>): List<Model> =
        domains.map { domain -> mapToData(domain) }
}