package adapter.spark

import com.clouway.mailservice.adapter.spark.MailEventHandler
import com.clouway.mailservice.core.Mailer
import org.eclipse.jetty.http.HttpStatus
import org.jmock.AbstractExpectations.returnValue
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.integration.junit4.JUnitRuleMockery
import org.junit.Rule
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as Is
import org.junit.Assert.assertThat
import spark.Request
import spark.Response
import sun.reflect.generics.reflectiveObjects.NotImplementedException

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MailSystemTest {

    data class TestEmailEvent(val email: String)
    
    @Rule
    @JvmField
    val context: JUnitRuleMockery = JUnitRuleMockery()
    
    private fun Mockery.expecting(block: Expectations.() -> Unit){
            checking(Expectations().apply(block))
    }

    private val mockMailer = context.mock(Mailer::class.java)

    private val mailHandler = MailEventHandler("::title::", "::content::", mockMailer)

    private val testEmailEvent = TestEmailEvent("::email::")

    private val req = object: Request(){

        override fun <T : Any?> attribute(attribute: String?): T {
            if(attribute == "event") return testEmailEvent as T
            throw NotImplementedException()
        }
    }

    private val res = object: Response() {

    }

    @Test
    fun handlerShouldSendMail(){

        context.expecting {
            oneOf(mockMailer).mail(testEmailEvent.email,
                    "::title::",
                    "::content::")
            will(returnValue(HttpStatus.OK_200))
        }

        assertThat(mailHandler.handle(req, res) as Int, Is(HttpStatus.OK_200))
    }
    
}