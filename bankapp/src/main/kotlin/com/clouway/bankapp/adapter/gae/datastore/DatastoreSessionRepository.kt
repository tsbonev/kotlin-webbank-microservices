package com.clouway.bankapp.adapter.gae.datastore

import com.clouway.bankapp.core.toUtilDate
import com.clouway.bankapp.core.*
import com.google.appengine.api.datastore.*
import com.google.appengine.api.datastore.FetchOptions.Builder.withLimit
import java.time.LocalDateTime
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class DatastoreSessionRepository(private val limit: Int = 100,
                                 private val instant: LocalDateTime = LocalDateTime.now(),
                                 private val sessionRefreshDays: Long = 10
) : SessionRepository, SessionClearer, SessionCounter {

    private fun mapEntityToSession(entity: Entity): Session{
        val typedEntity = TypedEntity(entity)
        return Session(
                typedEntity.longValue("userId"),
                typedEntity.string("sessionId"),
                typedEntity.dateTimeValueOrNull("expiresOn")!!,
                typedEntity.string("username"),
                typedEntity.booleanValueOr("isAuthenticated", false)
        )
    }

    private fun mapSessionToEntity(key: Key, session: Session): Entity{
        val typedEntity = TypedEntity(Entity(key))
        typedEntity.setIndexedDateTimeValue("expiresOn", session.expiresOn)
        typedEntity.setIndexedProperty("sessionId", session.sessionId)
        typedEntity.setIndexedProperty("userId", session.userId)
        typedEntity.setIndexedProperty("username", session.username)
        typedEntity.setUnindexedProperty("isAuthenticated", session.isAuthenticated)
        return typedEntity.raw()
    }

    private val service: DatastoreService
        get() = DatastoreServiceFactory.getDatastoreService()


    private fun greaterThanFilter(param: String, value: Any): Query.Filter {
        return Query.FilterPredicate(param,
                Query.FilterOperator.GREATER_THAN, value)
    }

    private fun getSessionList(date: LocalDateTime): List<Session> {
        val sessionEntities = service
                .prepare(Query("Session")
                        .setFilter(greaterThanFilter("expiresOn", date.toUtilDate())))
                .asList(withLimit(limit))

        val sessionList = mutableListOf<Session>()

        sessionEntities.forEach {
            sessionList.add(mapEntityToSession(it))
        }

        return sessionList
    }

    override fun issueSession(sessionRequest: SessionRequest) {
        val sessionKey = KeyFactory.createKey("Session", sessionRequest.sessionId)
        try {
            service.get(sessionKey)
        } catch (e: EntityNotFoundException) {

            val session = Session(
                    sessionRequest.userId,
                    sessionRequest.sessionId,
                    sessionRequest.expiration,
                    sessionRequest.username,
                    true
            )

            service.put(mapSessionToEntity(sessionKey, session))
        }
    }

    private fun refreshSession(sessionId: String) {
        val key = KeyFactory.createKey("Session", sessionId)

        try{
            val foundSession = service.get(key)
            val typedSession = TypedEntity(foundSession)
            val refreshedSession = Session(
                    typedSession.longValue("userId"),
                    typedSession.string("sessionId"),
                    instant.plusDays(sessionRefreshDays),
                    typedSession.string("username")
            )
            service.put(mapSessionToEntity(key, refreshedSession))
        }catch (e: EntityNotFoundException){
            throw SessionNotFoundException()
        }
    }

    override fun terminateSession(sessionId: String) {
        val key = KeyFactory.createKey("Session", sessionId)
        service.delete(key)
    }

    override fun deleteSessionsExpiringBefore(date: LocalDateTime) {
        val sessionList = getSessionList(date)

        for (session in sessionList) {
            val sessionKey = KeyFactory.createKey("Session", session.sessionId)
            service.delete(sessionKey)
        }
    }

    override fun getSessionAvailableAt(sessionId: String, date: LocalDateTime): Optional<Session> {

        val sessionKey = KeyFactory.createKey("Session", sessionId)

        return try {
            val sessionEntity = service.get(sessionKey)
            val typedSession = TypedEntity(sessionEntity)
            if (typedSession.dateTimeValueOrNull("expiresOn")
                            !!.isBefore(date)) return Optional.empty()

            refreshSession(sessionId)
            Optional.of(mapEntityToSession(sessionEntity))
        } catch (e: EntityNotFoundException) {
            Optional.empty()
        }
    }

    override fun getActiveSessionsCount(): Int {
        return service
                .prepare(Query("Session").setKeysOnly()
                        .setFilter(greaterThanFilter("expiresOn", instant.toUtilDate())))
                .asList(withLimit(limit)).size
    }
}