package com.vljx.hawkspeed.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.vljx.hawkspeed.data.database.entity.race.RaceLeaderboardEntity
import com.vljx.hawkspeed.data.network.serialisation.LocalDateDeserialiser
import com.vljx.hawkspeed.data.network.serialisation.LocalDateSerialiser
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

class Converters {
    // TODO: we instantiate our gson here, since injecting doesn't currently work in tests. To fix...
    //@Inject
    //lateinit var gson: Gson
    val gson: Gson =
        GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .enableComplexMapKeySerialization()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(LocalDate::class.java, LocalDateSerialiser())
            .registerTypeAdapter(LocalDate::class.java, LocalDateDeserialiser())
            .create()

    @TypeConverter
    fun fromLocalDate(value: String?) : LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun localDateToString(localDate: LocalDate?): String? {
        return localDate?.toString()
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

    @TypeConverter
    fun jsonObjectFromString(value: String?): JsonObject? {
        if(value == null) {
            return null
        }
        return JsonParser.parseString(value).asJsonObject
    }

    @TypeConverter
    fun stringFromJsonObject(value: JsonObject?): String? {
        if(value == null) {
            return null
        }
        return value.asString
    }

    @TypeConverter
    fun jsonArrayFromString(value: String?): JsonArray? {
        if(value == null) {
            return null
        }
        //val element = JsonParser.parseString(value)
        //return element.asJsonArray
        throw NotImplementedError()
    }

    @TypeConverter
    fun stringFromJsonArray(value: JsonArray?): String? {
        if(value == null) {
            return null
        }
        return gson.toJson(value)
    }

    @TypeConverter
    fun toTopLeaderboard(value: String?): List<RaceLeaderboardEntity>? =
        value?.let { jsonString ->
            gson.fromJson(value, Array<RaceLeaderboardEntity>::class.java).toList()
        }

    @TypeConverter
    fun fromTopLeaderboard(topLeaderboard: List<RaceLeaderboardEntity>?): String? {
        return topLeaderboard?.let { value ->
            gson.toJson(topLeaderboard.toTypedArray(), Array<RaceLeaderboardEntity>::class.java)
        }
    }
}