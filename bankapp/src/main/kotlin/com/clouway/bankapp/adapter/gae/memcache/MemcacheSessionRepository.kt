package com.clouway.bankapp.adapter.gae.memcache

import com.clouway.bankapp.core.*
import com.google.appengine.api.memcache.MemcacheService
import com.google.appengine.api.memcache.MemcacheServiceFactory
import java.time.LocalDateTime
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MemcacheSessionRepository(private val origin: SessionRepository,
                                private val serializer: JsonSerializer
) : SessionRepository {

    private val service: MemcacheService
        get() = MemcacheServiceFactory.getMemcacheService()

    override fun getSessionAvailableAt(sessionId: String, date: LocalDateTime): Optional<Session> {

        val cachedSession = service.getFromJson("sid_$sessionId", Session::class.java, serializer)
        return if (cachedSession.isPresent) {
            cachedSession
        } else {
            val persistentSession = origin.getSessionAvailableAt(sessionId, date)
            if(!persistentSession.isPresent) throw SessionNotFoundException()
            saveSessionInCache(persistentSession.get())
            persistentSession
        }

    }

    override fun issueSession(sessionRequest: SessionRequest): Session {
        val session = Session(
                sessionRequest.userId,
                sessionRequest.sessionId,
                sessionRequest.expiration,
                sessionRequest.username,
                sessionRequest.userEmail,
                true
        )
        saveSessionInCache(session)
        return origin.issueSession(sessionRequest)
    }

    override fun terminateSession(sessionId: String) {
        service.delete("sid_$sessionId")

        origin.terminateSession(sessionId)
    }

    private fun saveSessionInCache(session: Session) {
        service.putJson("sid_${session.sessionId}", session, serializer)
    }

    private fun <T> MemcacheService.getFromJson(key: String, typeOfT: Class<T>, serializer: JsonSerializer)
            : Optional<T> {
        val entity = this.get(key) ?: return Optional.empty()
        return Optional.of(serializer.fromJson(entity as String, typeOfT))
    }

    private fun MemcacheService.putJson(key: String, obj: Any, serializer: JsonSerializer) {
        val objToJson = serializer.toJson(obj)
        this.put(key, objToJson)
    }
}