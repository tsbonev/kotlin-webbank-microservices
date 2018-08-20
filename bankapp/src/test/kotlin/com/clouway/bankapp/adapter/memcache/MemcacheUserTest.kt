package com.clouway.bankapp.adapter.memcache

import com.clouway.bankapp.adapter.gae.memcache.MemcacheUserRepository
import com.clouway.bankapp.core.*
import com.google.appengine.api.memcache.MemcacheServiceFactory
import org.jmock.AbstractExpectations.returnValue
import org.jmock.AbstractExpectations.throwException
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.integration.junit4.JUnitRuleMockery
import org.junit.After
import org.junit.Rule
import org.junit.Test
import rule.MemcacheRule

import org.hamcrest.CoreMatchers.`is` as Is
import org.junit.Assert.assertThat
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MemcacheUserTest {

    @Rule
    @JvmField
    val helper: MemcacheRule = MemcacheRule()

    private fun Mockery.expecting(block: Expectations.() -> Unit){
        checking(Expectations().apply(block))
    }

    @Rule
    @JvmField
    val context: JUnitRuleMockery = JUnitRuleMockery()

    private val testId = UUID.randomUUID().toString()

    private val testUser = User(testId, "::username::", "::email::", "::password::")
    private val testRegistrationRequest = UserRegistrationRequest("::username::", "::email::", "::password::", testId)

    private val mockPersistentRepository = context.mock(UserRepository::class.java)

    private val userMemcacheRepo = MemcacheUserRepository(mockPersistentRepository)

    private val idPrefix = "user"

    @After
    fun cleanUp(){
        MemcacheServiceFactory.getMemcacheService().delete(key(testUser.id))
        MemcacheServiceFactory.getMemcacheService().delete(key(testUser.username))
    }

    @Test
    fun shouldRegisterUserInCache(){

        context.expecting {
            oneOf(mockPersistentRepository).registerIfNotExists(testRegistrationRequest)
            will(returnValue(testUser))
        }

        assertThat(userMemcacheRepo.registerIfNotExists(testRegistrationRequest), Is(testUser))
    }

    @Test (expected = UserAlreadyExistsException::class)
    fun shouldNotRegisterAUserTwice(){

        context.expecting {
            oneOf(mockPersistentRepository).registerIfNotExists(testRegistrationRequest)
            will(returnValue(testUser))

            oneOf(mockPersistentRepository).registerIfNotExists(testRegistrationRequest)
            will(throwException(UserAlreadyExistsException()))
        }

        userMemcacheRepo.registerIfNotExists(testRegistrationRequest)
        userMemcacheRepo.registerIfNotExists(testRegistrationRequest)
    }

    @Test
    fun shouldRetrieveUserFromCache(){

        context.expecting {
            oneOf(mockPersistentRepository).registerIfNotExists(testRegistrationRequest)
            will(returnValue(testUser))
        }

        userMemcacheRepo.registerIfNotExists(testRegistrationRequest)
        assertThat(userMemcacheRepo.getByUsername(testUser.username).get(), Is(testUser))
        assertThat(userMemcacheRepo.getById(testUser.id).get(), Is(testUser))
    }

    @Test
    fun shouldRetrieveUserFromPersistence(){

        context.expecting {
            oneOf(mockPersistentRepository).getByUsername(testUser.username)
            will(returnValue(Optional.of(testUser)))
            oneOf(mockPersistentRepository).getById(testUser.id)
            will(returnValue(Optional.of(testUser)))
        }

        assertThat(userMemcacheRepo.getByUsername(testUser.username).get(), Is(testUser))
        assertThat(userMemcacheRepo.getById(testUser.id).get(), Is(testUser))
    }

    @Test
    fun shouldReturnEmptyWhenNotFound(){

        context.expecting {
            oneOf(mockPersistentRepository).getByUsername(testUser.username)
            will(returnValue(Optional.empty<User>()))
            oneOf(mockPersistentRepository).getById(testUser.id)
            will(returnValue(Optional.empty<User>()))
        }

        assertThat(userMemcacheRepo.getByUsername(testUser.username).isPresent, Is(false))
        assertThat(userMemcacheRepo.getById(testUser.id).isPresent, Is(false))
    }

    @Test
    fun shouldUpdateUserInCache(){
        val updatedUser = User(testId, "::username::", "::new-email::", "::password::")

        context.expecting {
            oneOf(mockPersistentRepository).registerIfNotExists(testRegistrationRequest)
            will(returnValue(testUser))

            oneOf(mockPersistentRepository).update(updatedUser)
        }

        userMemcacheRepo.registerIfNotExists(testRegistrationRequest)
        userMemcacheRepo.update(updatedUser)
        assertThat(userMemcacheRepo.getByUsername(testUser.username).get().email, Is("::new-email::"))
        assertThat(userMemcacheRepo.getById(testUser.id).get().email, Is("::new-email::"))
    }

    @Test
    fun shouldDeleteUserFromCache(){

        context.expecting {
            oneOf(mockPersistentRepository).registerIfNotExists(testRegistrationRequest)
            will(returnValue(testUser))

            oneOf(mockPersistentRepository).deleteById(testUser.id)

            oneOf(mockPersistentRepository).getById(testUser.id)
            will(returnValue(Optional.empty<User>()))
            oneOf(mockPersistentRepository).getByUsername(testUser.username)
            will(returnValue(Optional.empty<User>()))
        }

        userMemcacheRepo.registerIfNotExists(testRegistrationRequest)
        userMemcacheRepo.deleteById(testUser.id)
        assertThat(userMemcacheRepo.getById(testUser.id).isPresent, Is(false))
        assertThat(userMemcacheRepo.getByUsername(testUser.username).isPresent, Is(false))
    }

    private fun key(key: Any): String{
        return "${idPrefix}_$key"
    }
}