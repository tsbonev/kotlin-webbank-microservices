package server

import com.clouway.bankapp.adapter.gae.datastore.DatastoreSessionRepository
import com.clouway.bankapp.adapter.gae.datastore.DatastoreTransactionRepository
import com.clouway.bankapp.adapter.gae.datastore.DatastoreUserRepository
import com.clouway.bankapp.adapter.gae.memcache.MemcacheSessionRepository
import com.clouway.bankapp.adapter.gae.memcache.MemcacheUserRepository
import com.clouway.bankapp.adapter.gae.pubsub.AsyncUserChangeListener
import com.clouway.bankapp.adapter.gae.pubsub.UserChangeListener
import com.clouway.bankapp.adapter.spark.*
import com.clouway.bankapp.core.GsonSerializer
import com.clouway.bankapp.core.Operation
import com.clouway.bankapp.core.User
import com.clouway.bankapp.core.security.MD5PasswordHasher
import com.clouway.bankapp.core.security.SecurityFilter
import com.clouway.bankapp.core.security.ThreadLocalSessionProvider
import com.clouway.pubsub.factory.EventBusFactory
import com.google.appengine.api.utils.SystemProperty
import spark.Filter
import spark.Route
import spark.Spark.*
import spark.kotlin.before
import spark.servlet.SparkApplication

class AppBootstrap : SparkApplication{
    override fun init() {

        val userChangeTopic = "user-change"

        val jsonSerializer = GsonSerializer()
        val responseTransformer = JsonResponseTransformer(jsonSerializer)
        val persistentUserRepo = DatastoreUserRepository()
        val cachedUserRepo = MemcacheUserRepository(persistentUserRepo)
        val sessionRepo = DatastoreSessionRepository()
        val transactionRepo = DatastoreTransactionRepository()
        val sessionProvider = ThreadLocalSessionProvider()

        val sessionLoader = MemcacheSessionRepository(sessionRepo)

        val openPaths = listOf(
                "/login",
                "/register",
                "/"
        )
        val forbiddenAfterLoginPaths = listOf(
                "/login",
                "/register"
        )

        val securityFilter = SecurityFilter(sessionLoader,
                sessionProvider,
                openPaths = openPaths,
                forbiddenAfterLoginPaths = forbiddenAfterLoginPaths)
        val passwordHasher = MD5PasswordHasher()

        val eventBus = EventBusFactory.createAsyncPubsubEventBus()
        val asyncEventListener = AsyncUserChangeListener(eventBus, userChangeTopic)

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

        val registerController = RegisterController(cachedUserRepo, jsonSerializer, passwordHasher, userChangeListeners)
        val listTransactionController = ListTransactionController(transactionRepo)
        val saveTransactionController = SaveTransactionController(transactionRepo, jsonSerializer, userChangeListeners)
        val loginController = LoginController(cachedUserRepo, sessionLoader, jsonSerializer, hasher = passwordHasher, listeners = userChangeListeners)
        val userController = UserController()
        val logoutController = LogoutController(sessionLoader, userChangeListeners)

        eventBus.createTopic(userChangeTopic)

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
    }
}