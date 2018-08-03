package rule

import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import org.junit.rules.ExternalResource

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MemcacheRule : ExternalResource() {

    private val helper = LocalServiceTestHelper(LocalMemcacheServiceTestConfig())

    override fun before() {
        helper.setUp()
    }

    override fun after() {
        helper.tearDown()
    }

}