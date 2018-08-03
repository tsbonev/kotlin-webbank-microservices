package server
import com.clouway.bankapp.adapter.gae.datastore.DatastoreSessionRepository
import com.clouway.bankapp.adapter.gae.datastore.DatastoreTransactionRepository
import com.clouway.bankapp.adapter.gae.datastore.DatastoreUserRepository
import com.clouway.bankapp.adapter.gae.memcache.MemcacheSessionRepository
import com.clouway.bankapp.adapter.spark.*
import com.clouway.bankapp.core.GsonSerializer
import com.clouway.bankapp.core.security.LoginFilter
import com.clouway.bankapp.core.security.SecurityFilter
import com.clouway.bankapp.core.security.ThreadLocalSessionProvider
import com.google.appengine.api.memcache.MemcacheServiceFactory
import spark.Filter
import spark.Route
import spark.Spark.*
import spark.kotlin.before
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

        val securityFilter = SecurityFilter(sessionLoader, sessionProvider)
        val loginFilter = LoginFilter(sessionProvider)

        val registerListener = RegisterListener()

        val registerController = RegisterController(userRepo, jsonSerializer)
        val listTransactionController = ListTransactionController(transactionRepo)
        val saveTransactionController = SaveTransactionController(transactionRepo, jsonSerializer)
        val loginController = LoginController(userRepo, sessionLoader, jsonSerializer)
        val userController = UserController()
        val logoutController = LogoutController(sessionLoader)

        registerController.addPropertyChangeListener(registerListener)

        before(Filter { req, res ->
            res.raw().characterEncoding = "UTF-8"
        })


        /*before(securityFilter)

        before("/login", loginFilter)
        before("/register", loginFilter)*/

        after(Filter {req, res ->
            res.type("application/json")
        })

        afterAfter {
            _, _ ->
            sessionProvider.clearContext()
        }

        post("/mail",
                AppController(MailController()),
                responseTransformer)

        get("/imail",
                AppController(InternalMailController()),
                responseTransformer)

        get("/user",
                SecuredController(userController, sessionProvider),
                responseTransformer)

        get("/transactions",
                SecuredController(listTransactionController, sessionProvider),
                responseTransformer)

        get("/active", Route{
            req, res ->
            return@Route sessionRepo.getActiveSessionsCount()
        }, responseTransformer)

        get("/statistics"){
            req, res ->
            val service = MemcacheServiceFactory.getMemcacheService()
            val stats = service.statistics
            responseTransformer.render(stats)
        }

        post("/transactions",
                SecuredController(saveTransactionController, sessionProvider),
                responseTransformer)

        post("/login",
                AppController(loginController),
                responseTransformer)

        post("/register",
                AppController(registerController),
                responseTransformer)

        post("/logout", SecuredController(logoutController, sessionProvider),
                responseTransformer)

    }
}