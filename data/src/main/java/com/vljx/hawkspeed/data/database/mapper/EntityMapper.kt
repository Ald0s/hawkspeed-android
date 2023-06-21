package com.vljx.hawkspeed.data.database.mapper

interface EntityMapper<Entity, Model> {
    fun mapFromEntity(entity: Entity): Model
    fun mapFromEntityList(entities: List<Entity>): List<Model> =
        entities.map { entity -> mapFromEntity(entity) }

    fun mapToEntity(model: Model): Entity
    fun mapToEntityList(models: List<Model>): List<Entity> =
        models.map { model ->
            mapToEntity(model)
        }
}