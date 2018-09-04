package com.clouway.bankapp.core

import com.clouway.bankapp.adapter.gae.datastore.DatastoreTransactionRepository
import com.clouway.bankapp.adapter.gae.datastore.DatastoreUserRepository
import com.clouway.bankapp.command.*
import com.clouway.bankapp.domain.Account
import com.clouway.bankapp.event.UserRegisteredEvent
import com.clouway.bankapp.event.AccountDepositEvent
import com.clouway.bankapp.event.AccountOpenedEvent
import com.clouway.bankapp.event.AccountWithdrawEvent
import com.clouway.bankapp.handler.UserRegisteredEventHandler
import com.clouway.bankapp.handler.AccountDepositEventHandler
import com.clouway.bankapp.handler.AccountOpenedEventHandler
import com.clouway.bankapp.handler.AccountWithdrawEventHandler
import com.clouway.kcqrs.core.SimpleMessageBus
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.Entity
import org.junit.*
import rule.DatastoreRule
import java.util.*
import org.hamcrest.CoreMatchers.`is` as Is
import org.junit.Assert.assertThat

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class KcqrsTest {

    private val messageBus = SimpleMessageBus()
    private val eventRepository = InMemoryAggregateRepository()
    private val userRepo = DatastoreUserRepository()
    private val transactionRepo = DatastoreTransactionRepository()

    @Rule
    @JvmField
    val dssHelper: DatastoreRule = DatastoreRule()

    @Before
    fun setUp() {
        messageBus.registerCommandHandler(RegisterUserCommand::class.java,
                RegisterUserCommandHandler(messageBus, userRepo))
        messageBus.registerEventHandler(UserRegisteredEvent::class.java,
                UserRegisteredEventHandler(userRepo))

        messageBus.registerCommandHandler(MakeDepositCommand::class.java,
                MakeDepositCommandHandler(eventRepository))
        messageBus.registerEventHandler(AccountDepositEvent::class.java,
                AccountDepositEventHandler(transactionRepo))

        messageBus.registerCommandHandler(MakeWithdrawCommand::class.java,
                MakeWithdrawCommandHandler(eventRepository))
        messageBus.registerEventHandler(AccountWithdrawEvent::class.java,
                AccountWithdrawEventHandler(transactionRepo))

        messageBus.registerCommandHandler(OpenAccountCommand::class.java,
                OpenAccountCommandHandler(eventRepository))
        messageBus.registerEventHandler(AccountOpenedEvent::class.java,
                AccountOpenedEventHandler(userRepo))
    }

    @Test
    fun happyPath(){

        val accountId = UUID.randomUUID().toString()

        messageBus.send(OpenAccountCommand("::userId::", accountId))

        messageBus.send(MakeDepositCommand(
                accountId,
                200.0
        ))

        messageBus.send(MakeWithdrawCommand(
                accountId,
                150.0
        ))

        assertThat(eventRepository.getById(UUID.fromString(accountId), Account::class.java).amount, Is(50.0))
    }

    @Test
    fun userOpensAccount() {

        messageBus.handle(UserRegisteredEvent(
                "::username::",
                "::email::",
                "::password::"
        ))

        val userId = userRepo.getByUsername("::username::").get().id
        val accountId = UUID.randomUUID().toString()

        messageBus.handle(AccountOpenedEvent(userId, accountId))

        messageBus.handle(AccountDepositEvent(
                accountId,
                200.0
        ))

        messageBus.handle(AccountWithdrawEvent(
                accountId,
                200.0
        ))

        assertThat(userRepo.getById(userId).isPresent, Is(true))
        assertThat(transactionRepo.getAccountTransactions(accountId).size, Is(2))
    }

    @Test
    fun registerUserByCommand(){
        messageBus.send(RegisterUserCommand("::username::",
                "::email::",
                "::password"))
        assertThat(userRepo.getByUsername("::username::").isPresent, Is(true))
    }

    @Test (expected = UserAlreadyExistsException::class)
    fun registerUserCommandValidatesRegistration(){
        val userEntity = Entity("User")
        userEntity.setIndexedProperty("id", "::userId::")
        userEntity.setIndexedProperty("username", "::username::")
        userEntity.setUnindexedProperty("email", "::email::")
        userEntity.setUnindexedProperty("password", "::password::")
        userEntity.setUnindexedProperty("accounts", emptyList<String>())

        DatastoreServiceFactory.getDatastoreService().put(userEntity)

        messageBus.send(RegisterUserCommand("::username::",
                "::email::",
                "::password"))
    }

    @Test(expected = InsufficientFundsException::class)
    fun cannotWithdrawMoraThanIsAvailable() {
        val userId = UUID.randomUUID().toString()
        val accountId = UUID.randomUUID().toString()

        messageBus.send(OpenAccountCommand(userId, accountId))

        messageBus.send(MakeWithdrawCommand(
                accountId,
                50.0
        ))
    }

}