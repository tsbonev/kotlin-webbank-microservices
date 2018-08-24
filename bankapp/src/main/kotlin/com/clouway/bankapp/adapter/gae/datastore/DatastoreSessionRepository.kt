package com.clouway.bankapp.adapter.gae.datastore

import com.clouway.bankapp.core.SessionClearer
import com.clouway.bankapp.core.SessionCounter
import com.clouway.bankapp.core.SessionRepository
import com.clouway.bankapp.core.*
import com.clouway.entityhelper.TypedEntity
import com.clouway.entityhelper.toUtilDate
import com.google.appengine.api.datastore.*
import com.google.appengine.api.datastore.FetchOptions.Builder.withLimit
import java.time.LocalDateTime
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class DatastoreSessionRepository(private val limit: Int = 100,
                                 private val getInstant: () -> LocalDateTime = {LocalDateTime.now()},
                                 private val sessionRefreshDays: Long = 10
) : SessionRepository, SessionClearer, SessionCounter {

    private val SESSION_KIND = "Session"

    private fun mapEntityToSession(entity: Entity): Session{
        val typedEntity = TypedEntity(entity)
        return Session(
                typedEntity.longValue("userId"),
                typedEntity.string("sessionId"),
                typedEntity.dateTimeValueOrNull("expiresOn")!!,
                typedEntity.string("username"),
                typedEntity.string("userEmail"),
                typedEntity.booleanValueOr("isAuthenticated", false)
        )
    }

    private fun mapSessionToEntity(key: Key, session: Session): Entity{
        val typedEntity = TypedEntity(Entity(key))
        typedEntity.setIndexedDateTimeValue("expiresOn", session.expiresOn)
        typedEntity.setIndexedProperty("sessionId", session.sessionId)
        typedEntity.setIndexedProperty("userId", session.userId)
        typedEntity.setIndexedProperty("username", session.username)
        typedEntity.setIndexedProperty("userEmail", session.userEmail)
        typedEntity.setUnindexedProperty("isAuthenticated", session.isAuthenticated)
        return typedEntity.raw()
    }

    private val service: DatastoreService
        get() = DatastoreServiceFactory.getDatastoreService()

    private fun getSessionList(date: LocalDateTime): List<Session> {
        val sessionEntities = service
                .prepare(Query(SESSION_KIND)
                        .setFilter(Query.FilterPredicate("expiresOn",
                                Query.FilterOperator.GREATER_THAN,
                                date.toUtilDate())))
                .asList(withLimit(limit))

        val sessionList = mutableListOf<Session>()

        sessionEntities.forEach {
            sessionList.add(mapEntityToSession(it))
        }

        return sessionList
    }

    override fun issueSession(sessionRequest: SessionRequest): Session {
        val sessionKey = KeyFactory.createKey(SESSION_KIND, sessionRequest.sessionId)
        try {
            val sessionEntity = service.get(sessionKey)
            return mapEntityToSession(sessionEntity)
        } catch (e: EntityNotFoundException) {

            val session = Session(
                    sessionRequest.userId,
                    sessionRequest.sessionId,
                    sessionRequest.expiration,
                    sessionRequest.username,
                    sessionRequest.userEmail,
                    true
            )

            service.put(mapSessionToEntity(sessionKey, session))
            return session
        }
    }

    private fun refreshSession(session: Session) {
        val key = KeyFactory.createKey(SESSION_KIND, session.sessionId)

        try{
            val refreshedSession = Session(
                    session.userId,
                    session.sessionId,
                    getInstant().plusDays(sessionRefreshDays),
                    session.username,
                    session.userEmail
            )
            service.put(mapSessionToEntity(key, refreshedSession))
        }catch (e: EntityNotFoundException){
            throw SessionNotFoundException()
        }
    }

    override fun terminateSession(sessionId: String) {
        val key = KeyFactory.createKey(SESSION_KIND, sessionId)
        service.delete(key)
    }

    override fun deleteSessionsExpiringBefore(date: LocalDateTime) {
        val sessionList = getSessionList(date)

        for (session in sessionList) {
            val sessionKey = KeyFactory.createKey(SESSION_KIND, session.sessionId)
            service.delete(sessionKey)
        }
    }

    override fun getSessionAvailableAt(sessionId: String, date: LocalDateTime): Optional<Session> {

        val sessionKey = KeyFactory.createKey(SESSION_KIND, sessionId)

        return try {
            val sessionEntity = service.get(sessionKey)
            val typedSession = TypedEntity(sessionEntity)
            if (typedSession.dateTimeValueOrNull("expiresOn")
                            !!.isBefore(date)) return Optional.empty()

            val session = mapEntityToSession(sessionEntity)

            if(session.expiresOn.isBefore(getInstant().plusDays(sessionRefreshDays))){
                refreshSession(session)
            }

            Optional.of(session)
        } catch (e: EntityNotFoundException) {
            Optional.empty()
        }
    }

    override fun getActiveSessionsCount(): Int {
        return service
                .prepare(Query(SESSION_KIND).setKeysOnly()
                        .setFilter(Query.FilterPredicate("expiresOn",
                                Query.FilterOperator.GREATER_THAN,
                                getInstant().toUtilDate())))
                .asList(withLimit(limit)).size
    }
}