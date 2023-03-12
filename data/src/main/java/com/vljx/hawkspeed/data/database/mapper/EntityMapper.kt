package com.vljx.hawkspeed.data.database.mapper

interface EntityMapper<Entity, Model> {
    fun mapFromEntity(entity: Entity): Model
    fun mapToEntity(model: Model): Entity
}