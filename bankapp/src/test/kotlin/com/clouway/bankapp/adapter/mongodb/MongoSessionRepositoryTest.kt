package com.clouway.bankapp.adapter.mongodb

import com.clouway.bankapp.core.Session
import com.clouway.bankapp.core.SessionRequest
import com.github.fakemongo.junit.FongoRule
import com.mongodb.MongoClient
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import org.hamcrest.CoreMatchers.`is` as Is
import org.junit.Assert.assertThat
import org.junit.Before
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MongoSessionRepositoryTest {

    @Rule
    @JvmField
    val fongoRule = FongoRule()

    lateinit var mongoClient: MongoClient

    lateinit var sessionRepo: MongoSessionRepository

    private val testId = UUID.randomUUID().toString()

    private val tomorrow =  LocalDateTime.of(2018, 8, 3, 10, 36, 23, 905000000)
    private val now = LocalDateTime.of(2018, 8, 2, 10, 36, 23, 905000000)
    private val yesterday =  LocalDateTime.of(2018, 8, 1, 10, 36, 23, 905000000)

    private val activeSessionRequest = SessionRequest(
            testId,
            "::sid::",
            "::username::",
            "::email::",
            tomorrow
    )

    private val expiredSessionRequest = SessionRequest(
            testId,
            "::sid_1::",
            "::username::",
            "::email::",
            yesterday
    )

    private val session = Session(
            testId,
            "::sid::",
            tomorrow,
            "::username::",
            "::email::",
            true
    )

    private val refreshDays = 10L

    @Before
    fun setUp(){
        mongoClient = fongoRule.mongoClient

        sessionRepo = MongoSessionRepository("test", mongoClient,
                sessionRefreshDays = refreshDays,
                getInstant = {now})
    }

    @Test
    fun shouldIssueSession(){
        assertThat(sessionRepo.issueSession(activeSessionRequest), Is(session))
    }

    @Test
    fun shouldRetrieveSession(){
        sessionRepo.issueSession(activeSessionRequest)

        assertThat(sessionRepo.getSessionAvailableAt(session.sessionId, now).get(), Is(session))
    }

    @Test
    fun shouldNotRetrieveExpiredSession(){
        sessionRepo.issueSession(expiredSessionRequest)

        assertThat(sessionRepo.getSessionAvailableAt(session.sessionId, now).isPresent, Is(false))
    }

    @Test
    fun shouldTerminateSession(){
        sessionRepo.issueSession(activeSessionRequest)

        sessionRepo.terminateSession(session.sessionId)

        assertThat(sessionRepo.getSessionAvailableAt(session.sessionId, now).isPresent, Is(false))
    }

    @Test
    fun shouldCountActiveSessions(){
        sessionRepo.issueSession(activeSessionRequest)
        sessionRepo.issueSession(SessionRequest(testId, "1234", "John",
                "email", tomorrow))
        sessionRepo.issueSession(expiredSessionRequest)

        assertThat(sessionRepo.getActiveSessionsCount(), Is(2L))
    }

    @Test
    fun shouldRefreshSession(){
        sessionRepo.issueSession(activeSessionRequest)
        sessionRepo.getSessionAvailableAt(session.sessionId, now)

        assertThat(sessionRepo.getSessionAvailableAt(session.sessionId, now).get()
                .expiresOn, Is(tomorrow.plusDays(refreshDays - 1)))
    }

    @Test
    fun shouldNotRefreshSessionMoreThanRefreshDays(){
        sessionRepo.issueSession(SessionRequest(testId, "::sid::", "John",
                "email", tomorrow.plusDays(refreshDays*2)))
        sessionRepo.getSessionAvailableAt(session.sessionId, now)

        assertThat(sessionRepo.getSessionAvailableAt(session.sessionId, now).get()
                .expiresOn, Is(tomorrow.plusDays(refreshDays*2)))
    }

}