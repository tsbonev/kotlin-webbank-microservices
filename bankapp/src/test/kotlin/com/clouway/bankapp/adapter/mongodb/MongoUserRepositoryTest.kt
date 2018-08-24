package com.clouway.bankapp.adapter.mongodb

import com.clouway.bankapp.core.User
import com.clouway.bankapp.core.UserAlreadyExistsException
import com.clouway.bankapp.core.UserRegistrationRequest
import com.github.fakemongo.junit.FongoRule
import com.mongodb.MongoClient
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as Is
import org.junit.Assert.assertThat
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MongoUserRepositoryTest {

    @Rule
    @JvmField
    val fongoRule = FongoRule()

    lateinit var mongoClient: MongoClient

    lateinit var userRepo: MongoUserRepository

    private val testId = UUID.randomUUID().toString()
    private val registerRequest = UserRegistrationRequest("John", "email", "password",testId)
    private val user = User(testId, "John", "email", "password")

    @Before
    fun setUp(){
        mongoClient = fongoRule.mongoClient
        userRepo = MongoUserRepository("test", mongoClient)
    }

    @Test
    fun shouldRegisterUser(){
        val registeredUser = userRepo.registerIfNotExists(registerRequest)
        assertThat(registeredUser.username, Is(user.username))
        assertThat(registeredUser.password, Is(user.password))
        assertThat(registeredUser.email, Is(user.email))
    }

    @Test (expected = UserAlreadyExistsException::class)
    fun shouldNotRegisterUserTwice(){
        userRepo.registerIfNotExists(registerRequest)
        userRepo.registerIfNotExists(registerRequest)
    }

    @Test
    fun shouldRetrieveUserById(){
        val registeredUser = userRepo.registerIfNotExists(registerRequest)

        assertThat(userRepo.getById(registeredUser.id).get(), Is(registeredUser))
    }

    @Test
    fun shouldRetrieveUserByUsername(){
        val registeredUser = userRepo.registerIfNotExists(registerRequest)

        assertThat(userRepo.getByUsername(registeredUser.username).get(), Is(registeredUser))
    }

    @Test
    fun shouldDeleteUser(){
        val registeredUser = userRepo.registerIfNotExists(registerRequest)
        userRepo.deleteById(registeredUser.id)
        assertThat(userRepo.getById(registeredUser.id).isPresent, Is(false))
    }

    @Test
    fun shouldUpdateUser(){
        val registeredUser = userRepo.registerIfNotExists(registerRequest)
        val updatedUser = User(
                registeredUser.id,
                registeredUser.username,
                "::new_email::",
                registeredUser.password
        )
        userRepo.update(updatedUser)
        assertThat(userRepo.getById(registeredUser.id).get(), Is(updatedUser))
    }


}