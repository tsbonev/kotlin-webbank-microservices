package com.clouway.bankapp.adapter.gae.datastore

import com.clouway.bankapp.core.*
import com.clouway.entityhelper.TypedEntity
import com.google.appengine.api.datastore.*
import com.google.appengine.api.datastore.FetchOptions.Builder.withLimit
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class DatastoreUsers : Users {

    private val USER_KIND = "User"

    private val service: DatastoreService
        get() = DatastoreServiceFactory.getDatastoreService()

    private fun checkIfUserExists(username: String): Boolean {
        return service.prepare(Query(USER_KIND)
                        .setFilter(Query.FilterPredicate("username",
                                Query.FilterOperator.EQUAL,
                                username)))
                        .asList(withLimit(1))
                        .size != 0
    }

    override fun getById(id: String): Optional<User> {
        val key = KeyFactory.createKey(USER_KIND, id)

        return try {
            val userEntity = service.get(key)
            Optional.of(mapEntityToUser(userEntity))
        }catch (e: EntityNotFoundException){
            Optional.empty()
        }
    }

    override fun deleteById(id: String) {
        val key = KeyFactory.createKey(USER_KIND, id)
        service.delete(key)
    }

    override fun update(user: User) {
        val key = KeyFactory.createKey(USER_KIND, user.id)
        service.put(mapUserToEntity(key, user))
    }

    override fun getByUsername(username: String): Optional<User> {

        val entity = service
                .prepare(Query(USER_KIND)
                        .setFilter(Query.FilterPredicate("username",
                                Query.FilterOperator.EQUAL,
                                username)))
                .asSingleEntity() ?: return Optional.empty()

        return Optional.of(mapEntityToUser(entity))
    }

    override fun registerIfNotExists(registerRequest: UserRegistrationRequest): User {

        val user = User(registerRequest.id ?: UUID.randomUUID().toString(),
                registerRequest.username,
                registerRequest.email,
                registerRequest.password)

        if (checkIfUserExists(registerRequest.username)) {
            throw UserAlreadyExistsException()
        }
        val userKey = KeyFactory.createKey(USER_KIND, user.id)

        service.put(mapUserToEntity(userKey, user))

        return user
    }

    private fun mapEntityToUser(entity: Entity): User{
        val typedEntity = TypedEntity(entity)
        return User(
                typedEntity.string("id"),
                typedEntity.string("username"),
                typedEntity.string("email"),
                typedEntity.stringOr("password", "")
        )
    }

    private fun mapUserToEntity(key: Key, user: User): Entity{
        val typedEntity = TypedEntity(Entity(key))
        typedEntity.setUnindexedProperty("password", user.password)
        typedEntity.setIndexedProperty("id", user.id)
        typedEntity.setIndexedProperty("username", user.username)
        typedEntity.setIndexedProperty("email", user.email)
        return typedEntity.raw()
    }
}