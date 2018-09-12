package rule

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import org.junit.rules.ExternalResource
import spark.Filter
import spark.Spark
import spark.servlet.SparkApplication
import java.io.File

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class SparkGaeDatastoreRule(private val sparkApplication: SparkApplication, private val envAppId: String) : ExternalResource() {
    constructor(sparkApplication: SparkApplication) : this(sparkApplication, "")

    private val dssConfig = LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy().setNoStorage(false)
            .setBackingStoreLocation("tmp/local_db.bin")
    private val datastoreHepler = LocalServiceTestHelper(dssConfig)

    override fun before() {
        File(dssConfig.backingStoreLocation).delete()

        Spark.before(Filter { request, _ ->
            datastoreHepler.setUp()
        })

        Spark.after(Filter { _, _ -> datastoreHepler.tearDown() })

        if (envAppId.isNotBlank()) datastoreHepler.setEnvAppId(envAppId)

        sparkApplication.init()
        Spark.awaitInitialization()
    }

    override fun after() {
        Spark.stop()
        File(dssConfig.backingStoreLocation).delete()
        Thread.currentThread().join(100)
    }
}