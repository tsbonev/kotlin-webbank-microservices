package rule

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import org.junit.rules.ExternalResource

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class DatastoreRule : ExternalResource() {

    private val helper = LocalServiceTestHelper(LocalDatastoreServiceTestConfig())

    override fun before() {
        helper.setUp()
    }

    override fun after() {
        helper.tearDown()
    }
}