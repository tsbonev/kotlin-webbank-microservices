package com.clouway.bankapp.core

import java.util.Optional

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class UsersDecorator(private val cacheService: Cache<User>,
                     private val decorated: Users) : Users{

    /**
     * Returns a user by id from the cache, if
     * the user is not found in the cache but found in persistence
     * then the user is cached before being returned.
     *
     * @param id The id of the user
     * @return An optional User
     */
    override fun getById(id: String): Optional<User> {
        val cachedUser = cacheService.get(id)
        return if(cachedUser.isPresent) cachedUser
        else{
            val persistentUser = decorated.getById(id)
            if(persistentUser.isPresent) cacheService.put(persistentUser.get())
            persistentUser
        }
    }

    /**
     * Returns a user by username from the cache, if
     * the user is not found in the cache but found in persistence
     * then the user is cached before being returned.
     *
     * @param username The username of the user
     * @return An optional User
     */
    override fun getByUsername(username: String): Optional<User> {
        val cachedUser = cacheService.get(username)
        return if(cachedUser.isPresent) cachedUser
        else{
            val persistentUser = decorated.getByUsername(username)
            if(persistentUser.isPresent) cacheService.put(persistentUser.get())
            persistentUser
        }
    }

    /**
     * Deletes a user by id.
     *
     * @param id The id of the user to delete
     */
    override fun deleteById(id: String) {
        decorated.deleteById(id)
        cacheService.remove(id)
    }

    /**
     * Updates a user in persistence and in the cache.
     *
     * @param user The updates User
     */
    override fun update(user: User) {
        decorated.update(user)
        cacheService.put(user)
    }

    /**
     * Registers a user and caches him, or throws a UserAlreadyExistsException.
     *
     * @param registerRequest The registration request
     * @return The registered User
     */
    @Throws(UserAlreadyExistsException::class)
    override fun registerIfNotExists(registerRequest: UserRegistrationRequest): User {
        val registeredUser = decorated.registerIfNotExists(registerRequest)
        cacheService.put(registeredUser)
        return registeredUser
    }
}