package adapter.spark

import com.clouway.mailservice.adapter.spark.MailEventHandler
import com.clouway.mailservice.core.Mailer
import com.clouway.mailservice.core.SendGridMailer
import com.clouway.pubsub.core.event.Event
import com.clouway.pubsub.core.event.EventWithAttributes
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

    data class TestEmailEvent(val email: String) : Event
    
    @Rule
    @JvmField
    val context: JUnitRuleMockery = JUnitRuleMockery()
    
    private fun Mockery.expecting(block: Expectations.() -> Unit){
            checking(Expectations().apply(block))
    }

    private val mockMailer = context.mock(Mailer::class.java)

    private val mailHandler = MailEventHandler("::title::", "::content::", mockMailer)

    private val testEmailEvent = TestEmailEvent("::email::")

    @Test
    fun handlerShouldSendMail(){

        context.expecting {
            oneOf(mockMailer).mail(testEmailEvent.email,
                    "::title::",
                    "::content::")
            will(returnValue(HttpStatus.OK_200))
        }

        assertThat(mailHandler.handle(EventWithAttributes(testEmailEvent,
                emptyMap())) as Int, Is(HttpStatus.OK_200))
    }

}