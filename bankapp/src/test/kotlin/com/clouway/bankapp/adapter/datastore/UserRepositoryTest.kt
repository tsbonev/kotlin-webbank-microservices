package com.clouway.bankapp.adapter.datastore

import com.clouway.bankapp.adapter.gae.datastore.DatastoreUserRepository
import com.clouway.bankapp.core.User
import com.clouway.bankapp.core.UserAlreadyExistsException
import com.clouway.bankapp.core.UserRegistrationRequest
import org.junit.Test
import org.junit.Assert.assertThat
import org.junit.Rule
import rule.DatastoreRule
import java.util.*
import org.hamcrest.CoreMatchers.`is` as Is

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class UserRepositoryTest {

    @Rule
    @JvmField
    val helper: DatastoreRule = DatastoreRule()

    private val userRepo = DatastoreUserRepository()

    private val testId = UUID.randomUUID().toString()
    private val registerJohn = UserRegistrationRequest("John", "email", "password")
    private val userJohn = User(testId, "John", "email", "password", listOf("::accountId::"))

    @Test
    fun shouldRegisterUser(){

        val user = userRepo.register(registerJohn)

        assertThat(userRepo.getById(user.id).get() == user, Is(true))

    }

    @Test
    fun shouldGetByUsername(){

        val user = userRepo.register(registerJohn)

        assertThat(userRepo.getByUsername(registerJohn.username).get(),
                Is(user))

    }

    @Test
    fun shouldNotFindByUsername(){

        assertThat(userRepo.getByUsername(userJohn.username).isPresent, Is(false))

    }

    @Test
    fun verifyCorrectPassword(){

        userRepo.register(UserRegistrationRequest("John", "email", "password"))

        assertThat(userRepo.checkPassword(userJohn), Is(true))
    }

    @Test
    fun invalidateIncorrectPassword(){

        val userJohn = User(testId, "John", "email", "incorrect password", emptyList())

        userRepo.register(UserRegistrationRequest("John", "email", "password"))

        assertThat(userRepo.checkPassword(userJohn), Is(false))
    }

    @Test
    fun shouldDeleteUser(){

        val user = userRepo.register(UserRegistrationRequest("John", "email", "password"))

        assertThat(userRepo.getById(user.id).isPresent, Is(true))

        userRepo.deleteById(user.id)
        assertThat(userRepo.getById(user.id).isPresent, Is(false))
    }

    @Test
    fun shouldUpdateUser(){

        userRepo.register(UserRegistrationRequest("John", "email", "password"))

        val userJohn = User(testId, "Don", "email", "password", emptyList())

        userRepo.update(userJohn)

        assertThat(userRepo.getById(testId).get().username, Is("Don"))
    }
}