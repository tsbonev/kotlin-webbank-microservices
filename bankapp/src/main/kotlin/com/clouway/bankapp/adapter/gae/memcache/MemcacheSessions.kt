package com.clouway.bankapp.adapter.gae.memcache

import com.clouway.bankapp.core.Cache
import com.clouway.bankapp.core.Session
import com.clouway.entityhelper.TypedEntity
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.memcache.MemcacheService
import com.google.appengine.api.memcache.MemcacheServiceFactory
import java.util.Optional

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MemcacheSessions: Cache<Session> {

    private val ID_PREFIX = "sid"
    private val SESSION_KIND = "Session"

    private val service: MemcacheService
        get() = MemcacheServiceFactory.getMemcacheService()

    override fun put(obj: Session): Session {
        val sessionEntity = mapSessionToEntity(obj)
        service.put(prefixKey(obj.sessionId), sessionEntity)
        return obj
    }

    override fun get(key: String): Optional<Session> {
        val cachedSession = service.get(prefixKey(key)) ?: return Optional.empty()

        return Optional.of(mapEntityToSession(cachedSession as Entity))
    }

    override fun remove(key: String) {
        service.delete(prefixKey(key))
    }

    private fun prefixKey(key: Any): String{
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