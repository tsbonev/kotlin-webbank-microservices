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
                                private val transformer: JsonSerializer
) : SessionRepository {

    private fun <T> MemcacheService.getFromJson(key: String, typeOfT: Class<T>, transformer: JsonSerializer): Optional<T> {
        val entity = this.get(key) ?: return Optional.empty()
        return Optional.of(transformer.fromJson(entity as String, typeOfT))
    }

    private fun MemcacheService.putJson(key: String, obj: Any, transformer: JsonSerializer) {
        val objToJson = transformer.toJson(obj)
        this.put(key, objToJson)
    }

    private val service: MemcacheService
        get() = MemcacheServiceFactory.getMemcacheService()

    override fun getSessionAvailableAt(sessionId: String, date: LocalDateTime): Optional<Session> {

        val cachedSession = service.getFromJson("sid_$sessionId", Session::class.java, transformer)
        return if (cachedSession.isPresent) {
            cachedSession
        } else {
            val persistentSession = origin.getSessionAvailableAt(sessionId, date)
            if(!persistentSession.isPresent) throw SessionNotFoundException()
            saveSessionInCache(persistentSession.get())
            persistentSession
        }

    }

    override fun issueSession(sessionRequest: SessionRequest) {
        val session = Session(
                sessionRequest.userId,
                sessionRequest.sessionId,
                sessionRequest.expiration,
                sessionRequest.username,
                true
        )
        saveSessionInCache(session)
        origin.issueSession(sessionRequest)
    }

    override fun terminateSession(sessionId: String) {
        service.delete("sid_$sessionId")

        origin.terminateSession(sessionId)
    }

    private fun saveSessionInCache(session: Session) {
        service.putJson("sid_${session.sessionId}", session, transformer)
    }
}