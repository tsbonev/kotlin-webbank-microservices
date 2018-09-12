package com.clouway.bankapp.adapter.gae.memcache

import com.clouway.bankapp.core.Cache
import com.clouway.bankapp.core.User
import com.clouway.entityhelper.TypedEntity
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.memcache.MemcacheService
import com.google.appengine.api.memcache.MemcacheServiceFactory
import java.util.Optional

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MemcacheUsers : Cache<User> {

    private val ID_PREFIX = "user"
    private val USER_KIND = "User"

    private val service: MemcacheService
        get() = MemcacheServiceFactory.getMemcacheService()

    override fun put(obj: User): User {
        val userEntity = mapUserToEntity(obj)
        service.put(prefixKey(obj.id), userEntity)
        service.put(prefixKey(obj.username), userEntity)
        return obj
    }

    override fun get(key: String): Optional<User> {
        val cachedUser = service.get(prefixKey(key)) ?: return Optional.empty()
        return Optional.of(mapEntityToUser(cachedUser as Entity))
    }

    override fun remove(key: String) {
        val cachedUser = mapEntityToUser(service.get(prefixKey(key)) as Entity)
        service.delete(prefixKey(cachedUser.username))
        service.delete(prefixKey(cachedUser.id))
    }

    private fun prefixKey(key: Any): String{
        return "${ID_PREFIX}_$key"
    }

    private fun mapEntityToUser(entity: Entity): User {
        val typedEntity = TypedEntity(entity)
        return User(
                typedEntity.string("id"),
                typedEntity.string("username"),
                typedEntity.string("email"),
                typedEntity.string("password")
        )
    }

    private fun mapUserToEntity(user: User): Entity {
        val userEntity = Entity(USER_KIND, user.id)
        val typedEntity = TypedEntity(userEntity)
        typedEntity.setUnindexedProperty("id", user.id)
        typedEntity.setUnindexedProperty("username", user.username)
        typedEntity.setUnindexedProperty("email", user.email)
        typedEntity.setUnindexedProperty("password", user.password)
        return typedEntity.raw()
    }
}