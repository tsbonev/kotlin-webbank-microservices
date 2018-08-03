package com.clouway.bankapp.adapter.gae.datastore

import com.google.appengine.api.datastore.Blob
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.Text
import com.google.appengine.repackaged.com.google.gson.Gson
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class TypedEntity(private val entity: Entity) {
    private val gson = Gson()
    private val indexWriter = IndexWriter()

    val key = entity.key

    fun double(key: String): Double {
        val value = entity.getProperty(key)
        return value as? Double ?: 0.0
    }

    fun string(key: String): String {
        val value = entity.getProperty(key)
        return value as? String ?: (value as Text).value
    }

    fun stringOr(key: String, defaultValue: String): String {
        val value = entity.getProperty(key) ?: return defaultValue
        return value as? String ?: (value as Text).value
    }

    fun stringOrNull(key: String): String? {
        val value = entity.getProperty(key) ?: return null
        return value as? String ?: (value as Text).value
    }

    fun longValue(key: String): Long {
        return entity.getProperty(key) as Long
    }

    fun longValueOrNull(key: String): Long? {
        val value = entity.getProperty(key)
        return if (value != null) value as Long else null
    }

    fun longValueOr(key: String, defaultValue: Long): Long {
        val value = entity.getProperty(key) ?: return defaultValue
        return value as Long
    }

    fun booleanValue(key: String): Boolean {
        val value = entity.getProperty(key) ?: false
        return value as Boolean
    }

    fun booleanValueOr(key: String, default: Boolean): Boolean {
        val value = entity.getProperty(key) ?: return default
        return value as Boolean
    }

    fun date(key: String): Date? {
        return if (entity.getProperty(key) == null) null else entity.getProperty(key) as Date
    }

    fun dateValue(key: String): Date = entity.getProperty(key) as Date

    fun dateValueOr(key: String, defaultValue: Date): Date {
        return if (entity.getProperty(key) == null) defaultValue else entity.getProperty(key) as Date
    }

    fun dateTimeValueOrNull(key: String): LocalDateTime? {
        val date = if (entity.getProperty(key) == null) return null else entity.getProperty(key) as Date
        return LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC)
    }

    fun setUnindexedDateTimeValue(key: String, dateTime: LocalDateTime) {
        val date = Date(dateTime.toInstant(ZoneOffset.UTC).toEpochMilli())
        entity.setUnindexedProperty(key, date)
    }

    fun setIndexedDateTimeValue(key: String, dateTime: LocalDateTime) {
        val date = Date(dateTime.toInstant(ZoneOffset.UTC).toEpochMilli())
        entity.setIndexedProperty(key, date)
    }

    fun byteArrayOr(propertyName: String, orValue: ByteArray): ByteArray {
        val result = entity.getProperty(propertyName) ?: return orValue
        return (result as Blob).bytes ?: orValue
    }

    fun <T> jsonValueOr(property: String, type: Type, defaultValue: T): T {
        if (!entity.hasProperty(property)) return defaultValue

        val json = entity.getProperty(property) as Text
        return gson.fromJson<T>(json.value, type) ?: defaultValue
    }

    fun <T> setJsonValue(property: String, value: T) {
        val textValue = gson.toJson(value)
        entity.setUnindexedProperty(property, Text(textValue))
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> list(propertyName: String): List<T> {
        if (!entity.hasProperty(propertyName)) return listOf()

        val value = (entity.getProperty(propertyName) ?: return listOf()) as? kotlin.collections.Collection<*>
                ?: throw IllegalStateException(String.format("Property '%s' of entity '%s' is not of type collection.", propertyName, entity.kind))

        if (value is List<*>) return value as List<T>
        return (value as kotlin.collections.Collection<T>).toList()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> set(propertyName: String): Set<T> {
        if (!entity.hasProperty(propertyName)) return emptySet()

        val o = (entity.getProperty(propertyName) ?: return setOf()) as? kotlin.collections.Collection<*>
                ?: throw IllegalStateException(String.format("Property '%s' of raw '%s' is not of type collection.", propertyName, entity.kind))

        if (o is kotlin.collections.Set<*>) return o as Set<T>
        return (o as kotlin.collections.Collection<T>).toSet()
    }

    fun setUnindexedProperty(key: String, value: Any) {

        if (value is String && value.toString().toByteArray(Charsets.UTF_8).size >= 1500) {
            entity.setUnindexedProperty(key, Text(value.toString()))
            return
        }
        entity.setUnindexedProperty(key, value)
    }

    fun setSearchIndexProperty(values: List<String>, wholeValues: List<String> = emptyList(), propertyName: String = "searchIndex") {
        entity.setIndexedProperty(propertyName, indexWriter.createIndex(*values.toTypedArray()).plus(wholeValues))
    }

    //Datastore has a limitation of 1500 bytes for a field.
    //If the provided field is a string with more than 1500 bytes it will be unindexed.
    fun setIndexedProperty(key: String, value: Any?) {
        if (value is String && value.toString().toByteArray(Charsets.UTF_8).size >= 1500) {
            entity.setUnindexedProperty(key, Text(value.toString()))
            return
        }
        entity.setIndexedProperty(key, value)
    }

    fun raw(): Entity = entity

}