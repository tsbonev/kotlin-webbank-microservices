package server

import com.clouway.bankapp.adapter.gae.datastore.DatastoreSessionRepository
import com.clouway.bankapp.adapter.gae.datastore.DatastoreTransactionRepository
import com.clouway.bankapp.adapter.gae.datastore.DatastoreUserRepository
import com.clouway.bankapp.adapter.gae.memcache.MemcacheSessionRepository
import com.clouway.bankapp.adapter.gae.pubsub.AsyncUserChangeListener
import com.clouway.bankapp.adapter.gae.pubsub.UserChangeListener
import com.clouway.bankapp.adapter.spark.*
import com.clouway.bankapp.command.*
import com.clouway.bankapp.core.GsonSerializer
import com.clouway.bankapp.core.Operation
import com.clouway.bankapp.core.User
import com.clouway.bankapp.core.security.SecurityFilter
import com.clouway.bankapp.core.security.ThreadLocalSessionProvider
import com.clouway.bankapp.event.UserRegisteredEvent
import com.clouway.bankapp.event.AccountDepositEvent
import com.clouway.bankapp.event.AccountOpenedEvent
import com.clouway.bankapp.event.AccountWithdrawEvent
import com.clouway.bankapp.handler.UserRegisteredEventHandler
import com.clouway.bankapp.handler.AccountDepositEventHandler
import com.clouway.bankapp.handler.AccountOpenedEventHandler
import com.clouway.bankapp.handler.AccountWithdrawEventHandler
import com.clouway.pubsub.factory.EventBusFactory
import com.google.appengine.api.utils.SystemProperty
import spark.Filter
import spark.Route
import spark.Spark.*
import spark.kotlin.before
import spark.kotlin.post
import spark.servlet.SparkApplication

class AppBootstrap : SparkApplication{
    override fun init() {


        val jsonSerializer = GsonSerializer()
        val responseTransformer = JsonResponseTransformer(jsonSerializer)
        val userRepo = DatastoreUserRepository()
        val sessionRepo = DatastoreSessionRepository()
        val transactionRepo = DatastoreTransactionRepository()
        val sessionProvider = ThreadLocalSessionProvider()

        val sessionLoader = MemcacheSessionRepository(sessionRepo, jsonSerializer)

        val openPaths = listOf(
                "/login",
                "/register",
                "/",
                "/v1/register",
                "/_ah/admin",
                "/_ah/admin/datastore",
                "/_ah/admin/taskqueue",
                "/worker/kcqrs"
        )

        val forbiddenAfterLoginPaths = listOf(
                "/login",
                "/register"
        )

        val securityFilter = SecurityFilter(sessionLoader,
                sessionProvider,
                openPaths = openPaths,
                forbiddenAfterLoginPaths = forbiddenAfterLoginPaths)

        val eventPublisher = EventBusFactory.createAsyncPubsubEventBus()
        val asyncEventListener = AsyncUserChangeListener(eventPublisher)

        val userChangeListeners = object: UserChangeListener{
            val listeners = if(inProduction()) listOf(asyncEventListener) else emptyList()

            override fun onRegistration(user: User) {
                listeners.forEach { it.onRegistration(user) }
            }

            override fun onLogout(username: String, email: String) {
                listeners.forEach { it.onLogout(username, email) }
            }

            override fun onLogin(username: String) {
                listeners.forEach { it.onLogin(username) }
            }

            override fun onTransaction(username: String, amount: Double, action: Operation) {
                listeners.forEach { it.onTransaction(username, amount, action) }
            }

            private fun inProduction(): Boolean{
                return SystemProperty.environment.value() == SystemProperty.Environment.Value.Production
            }
        }

        val registerController = RegisterController(userRepo, jsonSerializer, userChangeListeners)
        val listTransactionController = ListTransactionController(transactionRepo)
        val saveTransactionController = SaveTransactionController(transactionRepo, jsonSerializer, userChangeListeners)
        val loginController = LoginController(userRepo, sessionLoader, jsonSerializer, listeners = userChangeListeners)
        val userController = UserController()
        val logoutController = LogoutController(sessionLoader, userChangeListeners)

        before(Filter { _, res ->
            res.raw().characterEncoding = "UTF-8"
        })


        before(securityFilter)

        after(Filter {_, res ->
            res.type("application/json")
        })

        afterAfter {
            _, _ ->
            sessionProvider.clearContext()
        }

        get("/user",
                SecuredController(userController, sessionProvider),
                responseTransformer)

        get("/transactions",
                SecuredController(listTransactionController, sessionProvider),
                responseTransformer)

        get("/active", Route{
            _, _ ->
            return@Route sessionRepo.getActiveSessionsCount()
        }, responseTransformer)


        post("/transactions",
                SecuredController(saveTransactionController, sessionProvider),
                responseTransformer)

        post("/login", Route{
            req, res ->
                return@Route loginController.handle(req, res)},
                responseTransformer)

        post("/register", Route{
            req, res ->
            return@Route registerController.handle(req, res)},
                responseTransformer)

        post("/logout", SecuredController(logoutController, sessionProvider),
                responseTransformer)

        val messageBus = CQRSContext.messageBus()
        val eventRepository = CQRSContext.eventRepository()

        messageBus.registerCommandHandler(RegisterUserCommand::class.java,
                RegisterUserCommandHandler(messageBus, userRepo))
        messageBus.registerEventHandler(UserRegisteredEvent::class.java,
                UserRegisteredEventHandler(userRepo))

        messageBus.registerCommandHandler(OpenAccountCommand::class.java,
                OpenAccountCommandHandler(eventRepository))
        messageBus.registerEventHandler(AccountOpenedEvent::class.java,
                AccountOpenedEventHandler(userRepo))

        messageBus.registerCommandHandler(MakeDepositCommand::class.java,
                MakeDepositCommandHandler(eventRepository))
        messageBus.registerEventHandler(AccountDepositEvent::class.java,
                AccountDepositEventHandler(transactionRepo))

        messageBus.registerCommandHandler(MakeWithdrawCommand::class.java,
                MakeWithdrawCommandHandler(eventRepository))
        messageBus.registerEventHandler(AccountWithdrawEvent::class.java,
                AccountWithdrawEventHandler(transactionRepo))

        post("/v1/register",
                RegisterUserHandler(messageBus, jsonSerializer),
                responseTransformer)

        post("/v1/account/new",
                SecuredController(OpenAccountHandler(messageBus), sessionProvider),
                responseTransformer)

        post("/v1/transactions",
                SecuredController(MakeTransactionHandler(messageBus, userRepo, jsonSerializer),
                        sessionProvider),
                responseTransformer)
    }
}