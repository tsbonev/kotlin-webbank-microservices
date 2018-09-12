package com.clouway.bankapp.adapter.memcache

import com.clouway.bankapp.adapter.gae.memcache.MemcacheUsers
import com.clouway.bankapp.core.*
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.integration.junit4.JUnitRuleMockery
import org.junit.Rule
import org.junit.Test
import rule.MemcacheRule

import org.hamcrest.CoreMatchers.`is` as Is
import org.junit.Assert.assertThat
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MemcacheUsersTest {

    @Rule
    @JvmField
    val helper: MemcacheRule = MemcacheRule()

    private val user = User("::id::", "::username::", "::email::", "::password::")

    private val usersCache = MemcacheUsers()

    @Test
    fun saveUserInCache(){

        assertThat(usersCache.put(user), Is(user))
    }

    @Test
    fun retrieveUserFromCache(){
        usersCache.put(user)

        assertThat(usersCache.get(user.username).get(), Is(user))
        assertThat(usersCache.get(user.id).get(), Is(user))
    }

    @Test
    fun returnEmptyWhenNotFound(){
        assertThat(usersCache.get(user.username).isPresent, Is(false))
        assertThat(usersCache.get(user.id).isPresent, Is(false))
    }

    @Test
    fun deleteUserFromCacheById(){
        usersCache.put(user)
        usersCache.remove(user.id)

        assertThat(usersCache.get(user.id).isPresent, Is(false))
        assertThat(usersCache.get(user.username).isPresent, Is(false))
    }

    @Test
    fun deleteUserFromCacheByUsername(){
        usersCache.put(user)
        usersCache.remove(user.username)

        assertThat(usersCache.get(user.id).isPresent, Is(false))
        assertThat(usersCache.get(user.username).isPresent, Is(false))
    }
}