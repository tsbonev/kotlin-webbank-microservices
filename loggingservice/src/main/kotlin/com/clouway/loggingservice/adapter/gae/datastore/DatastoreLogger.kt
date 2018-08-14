package com.clouway.loggingservice.adapter.gae.datastore

import com.clouway.entityhelper.TypedEntity
import com.clouway.entityhelper.toUtilDate
import com.clouway.loggingservice.core.Log
import com.clouway.loggingservice.core.Logger
import com.clouway.pubsub.core.event.Event
import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.FetchOptions.Builder.withLimit
import com.google.appengine.api.datastore.Query
import com.google.appengine.repackaged.com.google.gson.Gson
import jdk.nashorn.internal.runtime.regexp.joni.Config.log
import org.eclipse.jetty.http.HttpStatus
import java.time.LocalDateTime

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class DatastoreLogger(private val limit: Int = 100) : Logger {

    private val LOG_KIND = "Log"

    override fun getLogsBetween(from: LocalDateTime, to: LocalDateTime): List<Log> {

        val results = service.prepare(Query(LOG_KIND)
                .setFilter(Query.CompositeFilter(Query.CompositeFilterOperator.AND,
                    listOf(
                        Query.FilterPredicate("time", Query.FilterOperator.GREATER_THAN, from.toUtilDate()),
                        Query.FilterPredicate("time", Query.FilterOperator.LESS_THAN, to.toUtilDate())))))
                .asList(withLimit(limit))

        return mapEntitiesToList(results)
    }

    override fun getLogsFrom(time: LocalDateTime): List<Log> {
        val results = service.prepare(Query(LOG_KIND)
                .setFilter(Query.FilterPredicate("time", Query.FilterOperator.EQUAL, time.toUtilDate())))
                .asList(withLimit(limit))

        return mapEntitiesToList(results)
    }

    override fun storeLog(log: Log): Log {
        service.put(mapLogToEntity(log))
        return log
    }

    private fun mapLogToEntity(log: Log): Entity {
        val entity = Entity(LOG_KIND)
        val gson = Gson()
        val typedEntity = TypedEntity(entity)
        typedEntity.setUnindexedProperty("event", gson.toJson(log.event))
        typedEntity.setIndexedProperty("time", log.time.toUtilDate())
        typedEntity.setIndexedProperty("eventType", log.eventType.name)
        return typedEntity.raw()
    }

    private fun mapEntitiesToList(entityList: List<Entity>): List<Log> {
        val gson = Gson()
        val logList = mutableListOf<Log>()
        entityList.forEach {
            val typedEntity = TypedEntity(it)
            val eventType = Class.forName(typedEntity.string("eventType"))
            logList.add(Log(
                    gson.fromJson(typedEntity.string("event"), eventType) as Event,
                    eventType,
                    typedEntity.dateTimeValueOrNull("time")!!
            ))
        }
        return logList
    }

    private val service: DatastoreService
        get() = DatastoreServiceFactory.getDatastoreService()
}