package com.clouway.bankapp.adapter.gae.memcache

import com.clouway.bankapp.core.*
import com.clouway.entityhelper.TypedEntity
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.memcache.MemcacheService
import com.google.appengine.api.memcache.MemcacheServiceFactory
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MemcacheUsers(private val origin: Users) : Users {

    private val ID_PREFIX = "user"
    private val USER_KIND = "User"

    private val service: MemcacheService
        get() = MemcacheServiceFactory.getMemcacheService()

    override fun update(user: User) {
        origin.update(user)
        val userEntity = mapUserToEntity(user)
        service.put(key(user.username), userEntity)
        service.put(key(user.id), userEntity)
    }

    override fun getByUsername(username: String): Optional<User> {
        val userEntity = service.get(key(username)) ?: return origin.getByUsername(username)
        return Optional.of(mapEntityToUser(userEntity as Entity))
    }

    override fun getById(id: String): Optional<User> {
        val userEntity = service.get(key(id)) ?: return origin.getById(id)
        return Optional.of(mapEntityToUser(userEntity as Entity))
    }

    override fun deleteById(id: String) {
        val possibleUser = getById(id)

        if(possibleUser.isPresent){
            origin.deleteById(id)
            service.delete(key(possibleUser.get().username))
            service.delete(key(id))
        }
    }

    override fun registerIfNotExists(registerRequest: UserRegistrationRequest): User {
        val user = origin.registerIfNotExists(registerRequest)
        val userEntity = mapUserToEntity(user)
        service.put(key(user.username), userEntity)
        service.put(key(user.id), userEntity)
        return user
    }

    private fun key(key: Any): String{
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