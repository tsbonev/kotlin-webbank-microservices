package com.clouway.bankapp.core

import org.apache.http.annotation.NotThreadSafe
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as Is
import org.junit.Assert.assertThat

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class UsersDecoratorTest {

    private val usersCache = InMemoryUsersCache()
    private val usersRepository = InMemoryUsers()

    private val users = UsersDecorator(usersCache, usersRepository)

    private val registrationRequest = UserRegistrationRequest(
            "::username::",
            "::email::",
            "::password::"
    )

    private val user = User(
            "::id::",
            "::username::",
            "::email::",
            "::password::"
    )

    @Test
    fun returnRegisteredUser(){
        val registeredUser = users.registerIfNotExists(registrationRequest)
        assertThat(registeredUser.copy(id = "::id::"), Is(user))
    }

    @Test
    fun saveUserInCache(){
        val registeredUser = users.registerIfNotExists(registrationRequest)

        assertThat(usersCache.get(registeredUser.username).isPresent, Is(true))
        assertThat(usersCache.get(registeredUser.id).isPresent, Is(true))
    }

    @Test
    fun saveUserInPersistence(){
        val registeredUser = users.registerIfNotExists(registrationRequest)

        assertThat(usersRepository.getByUsername(registeredUser.username).isPresent, Is(true))
        assertThat(usersRepository.getById(registeredUser.id).isPresent, Is(true))
    }

    @Test(expected = UserAlreadyExistsException::class)
    fun rejectRegisteringExistingUser(){
        users.registerIfNotExists(registrationRequest)
        users.registerIfNotExists(registrationRequest)
    }

    @Test
    fun retrieveUserById(){
        val registeredUser = users.registerIfNotExists(registrationRequest)

        assertThat(users.getById(registeredUser.id).isPresent, Is(true))
        assertThat(users.getById(registeredUser.id).get(), Is(registeredUser))
    }

    @Test
    fun retrieveUserByUsername(){
        val registeredUser = users.registerIfNotExists(registrationRequest)

        assertThat(users.getByUsername(registeredUser.username).isPresent, Is(true))
        assertThat(users.getByUsername(registeredUser.username).get(), Is(registeredUser))
    }

    @Test
    fun saveUserInCacheIfRetrievedFromPersistence(){
        val registeredUser = users.registerIfNotExists(registrationRequest)

        usersCache.remove(registeredUser.id)
        users.getById(registeredUser.id)

        assertThat(usersCache.get(registeredUser.id).isPresent, Is(true))
    }

    @Test
    fun updateUser(){
        val registeredUser = users.registerIfNotExists(registrationRequest)
        val updatedUser = registeredUser.copy(email = "::new-email::")

        users.update(updatedUser)

        assertThat(users.getById(registeredUser.id).isPresent, Is(true))
        assertThat(users.getById(registeredUser.id).get(), Is(updatedUser))
    }

    @Test
    fun returnEmptyWhenNotFound(){
        assertThat(usersCache.get(user.id).isPresent, Is(false))
        assertThat(usersCache.get(user.username).isPresent, Is(false))
    }

    @Test
    fun deleteUser(){
        val registeredUser = users.registerIfNotExists(registrationRequest)

        users.deleteById(registeredUser.id)

        assertThat(users.getByUsername(registeredUser.username).isPresent, Is(false))
        assertThat(users.getByUsername(registeredUser.id).isPresent, Is(false))
    }
}