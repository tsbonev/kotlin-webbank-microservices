package com.clouway.bankapp.adapter.datastore

import com.clouway.bankapp.adapter.gae.datastore.DatastoreUserRepository
import com.clouway.bankapp.core.User
import com.clouway.bankapp.core.UserAlreadyExistsException
import com.clouway.bankapp.core.UserRegistrationRequest
import org.junit.Test
import org.junit.Assert.assertThat
import org.junit.Rule
import rule.DatastoreRule
import org.hamcrest.CoreMatchers.`is` as Is

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class UserRepositoryTest {

    @Rule
    @JvmField
    val helper: DatastoreRule = DatastoreRule()

    private val userRepo = DatastoreUserRepository()

    private val registerJohn = UserRegistrationRequest("John", "password")
    private val userJohn = User(1, "John", "password")

    @Test
    fun shouldRegisterUser(){

        val user = userRepo.registerIfNotExists(registerJohn)

        assertThat(userRepo.getById(user.id).get() == user, Is(true))

    }

    @Test(expected = UserAlreadyExistsException::class)
    fun shouldNotRegisterUserTwice(){

        userRepo.registerIfNotExists(registerJohn)
        userRepo.registerIfNotExists(registerJohn)

    }

    @Test
    fun shouldGetByUsername(){

        val user = userRepo.registerIfNotExists(registerJohn)

        assertThat(userRepo.getByUsername(registerJohn.username).get(),
                Is(user))

    }

    @Test
    fun shouldNotFindByUsername(){

        assertThat(userRepo.getByUsername(userJohn.username).isPresent, Is(false))

    }

    @Test
    fun verifyCorrectPassword(){

        userRepo.registerIfNotExists(UserRegistrationRequest("John", "password"))

        assertThat(userRepo.checkPassword(userJohn), Is(true))
    }

    @Test
    fun invalidateIncorrectPassword(){

        val userJohn = User(1, "John", "incorrect password")

        userRepo.registerIfNotExists(UserRegistrationRequest("John", "password"))

        assertThat(userRepo.checkPassword(userJohn), Is(false))
    }

    @Test
    fun shouldDeleteUser(){

        val user = userRepo.registerIfNotExists(UserRegistrationRequest("John", "password"))

        assertThat(userRepo.getById(user.id).isPresent, Is(true))

        userRepo.deleteById(user.id)
        assertThat(userRepo.getById(user.id).isPresent, Is(false))
    }

    @Test
    fun shouldUpdateUser(){

        userRepo.registerIfNotExists(UserRegistrationRequest("John", "password"))

        val userJohn = User(1, "Don", "password")

        userRepo.update(userJohn)

        assertThat(userRepo.getById(1).get().username, Is("Don"))
    }
}