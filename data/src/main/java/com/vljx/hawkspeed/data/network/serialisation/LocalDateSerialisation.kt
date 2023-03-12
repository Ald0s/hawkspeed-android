package com.vljx.hawkspeed.data.network.serialisation

import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDate

class LocalDateSerialiser: JsonSerializer<LocalDate?> {
    override fun serialize(
        src: LocalDate?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        if(src == null) {
            return JsonNull.INSTANCE
        }
        return JsonPrimitive(src.toString())
    }
}

class LocalDateDeserialiser: JsonDeserializer<LocalDate?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LocalDate? {
        if(json == null) {
            return null
        }
        return LocalDate.parse(json.asString)
    }
}