package com.clouway.bankapp.adapter.gae.memcache

import com.clouway.bankapp.core.*
import com.clouway.entityhelper.TypedEntity
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.memcache.MemcacheService
import com.google.appengine.api.memcache.MemcacheServiceFactory
import java.time.LocalDateTime
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MemcacheSessions(private val origin: Sessions)
    : Sessions {

    private val ID_PREFIX = "sid"
    private val SESSION_KIND = "Session"

    private val service: MemcacheService
        get() = MemcacheServiceFactory.getMemcacheService()

    override fun getSessionAvailableAt(sessionId: String, date: LocalDateTime): Optional<Session> {

        val cachedSession = service.get(key(sessionId)) ?: return origin.getSessionAvailableAt(sessionId, date)

        val mappedSession = mapEntityToSession(cachedSession as Entity)

        return if(mappedSession.expiresOn.isBefore(date)) {
            terminateSession(sessionId)
            Optional.empty()
        }
        else{
            Optional.of(mappedSession)
        }
    }

    override fun issueSession(sessionRequest: SessionRequest): Session {
        val session = origin.issueSession(sessionRequest)
        saveSessionInCache(session)
        return session
    }

    override fun terminateSession(sessionId: String) {
        origin.terminateSession(sessionId)

        service.delete(key(sessionId))
    }

    private fun saveSessionInCache(session: Session) {
        val sessionEntity = mapSessionToEntity(session)
        service.put(key(session.sessionId), sessionEntity)
    }

    private fun key(key: Any): String{
        return "${ID_PREFIX}_$key"
    }

    private fun mapEntityToSession(entity: Entity): Session{
        val typedSession = TypedEntity(entity)
        return Session(
                typedSession.string("userId"),
                typedSession.string("sessionId"),
                typedSession.dateTimeValueOrNull("expiresOn")!!,
                typedSession.string("username"),
                typedSession.string("userEmail"),
                typedSession.booleanValue("isAuthenticated")
        )
    }

    private fun mapSessionToEntity(session: Session): Entity {
        val sessionEntity = Entity(SESSION_KIND, session.sessionId)
        val typedSession = TypedEntity(sessionEntity)
        typedSession.setUnindexedProperty("sessionId", session.sessionId)
        typedSession.setUnindexedProperty("userId", session.userId)
        typedSession.setUnindexedProperty("username", session.username)
        typedSession.setUnindexedProperty("userEmail", session.userEmail)
        typedSession.setUnindexedDateTimeValue("expiresOn", session.expiresOn)
        typedSession.setUnindexedProperty("isAuthenticated", session.isAuthenticated)
        return typedSession.raw()
    }
}