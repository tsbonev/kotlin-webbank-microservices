package com.clouway.bankapp.adapter.datastore

import com.clouway.bankapp.adapter.gae.datastore.DatastoreUsers
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
class DatastoreUsersTest {

    @Rule
    @JvmField
    val helper: DatastoreRule = DatastoreRule()

    private val userRepo = DatastoreUsers()

    private val registrationRequest = UserRegistrationRequest("::username::", "::email::", "::password::")
    private val user = User("::userId::", "::username::", "::email::", "::password::")

    @Test
    fun returnRegisteredUser(){
        val user = userRepo.registerIfNotExists(registrationRequest)

        assertThat(userRepo.getById(user.id).get() == user, Is(true))
    }

    @Test(expected = UserAlreadyExistsException::class)
    fun rejectRegistrationOfExistingUser(){
        userRepo.registerIfNotExists(registrationRequest)
        userRepo.registerIfNotExists(registrationRequest)
    }

    @Test
    fun retrieveUserByUsername(){
        val user = userRepo.registerIfNotExists(registrationRequest)

        assertThat(userRepo.getByUsername(registrationRequest.username).get(),
                Is(user))
    }

    @Test
    fun returnEmptyWhenNotFound(){
        assertThat(userRepo.getByUsername(user.username).isPresent, Is(false))
        assertThat(userRepo.getById(user.id).isPresent, Is(false))
    }

    @Test
    fun deleteById(){
        val user = userRepo.registerIfNotExists(UserRegistrationRequest("John", "email", "password"))

        userRepo.deleteById(user.id)

        assertThat(userRepo.getById(user.id).isPresent, Is(false))
        assertThat(userRepo.getByUsername(user.username).isPresent, Is(false))
    }

    @Test
    fun updateUser(){
        val registeredUser = userRepo.registerIfNotExists(registrationRequest)

        val updatedUser = registeredUser.copy(email = "::new-email::")

        userRepo.update(updatedUser)

        assertThat(userRepo.getById(registeredUser.id).get(), Is(updatedUser))
    }
}