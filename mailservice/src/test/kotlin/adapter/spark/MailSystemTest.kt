package adapter.spark

import com.clouway.mailservice.adapter.spark.UserRegistrationHandler
import com.clouway.mailservice.core.Mailer
import com.clouway.pubsub.core.EventBus
import com.clouway.pubsub.core.event.Event
import com.clouway.pubsub.core.event.EventHandler
import com.clouway.pubsub.core.event.UserRegisteredEvent
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
import spark.Route
import sun.reflect.generics.reflectiveObjects.NotImplementedException

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MailSystemTest {
    
    @Rule
    @JvmField
    val context: JUnitRuleMockery = JUnitRuleMockery()
    
    private fun Mockery.expecting(block: Expectations.() -> Unit){
            checking(Expectations().apply(block))
    }

    private val mockMailer = context.mock(Mailer::class.java)

    private val mailHandler = UserRegistrationHandler(mockMailer)

    private val fakeEventBus = object: EventBus{
        override fun publish(event: Event, eventType: Class<*>, topic: String) {
            throw NotImplementedException()
        }

        override fun register(handlers: Map<Class<*>, EventHandler>): Route {
            return Route { _, _ ->
                req.attribute("event", testUserRegisteredEvent)
                mailHandler.handle(req, res)
            }
        }

        override fun subscribe(topic: String, subscription: String, endpoint: String) {
            throw NotImplementedException()
        }

    }

    private val testUserRegisteredEvent = UserRegisteredEvent(123, "::username::", "::email::")
    private lateinit var attributeHolder: Event

    private val req = object: Request(){
        override fun attribute(attribute: String, value: Any) {
            attributeHolder = value as Event
        }

        override fun <T : Any?> attribute(attribute: String?): T {
            return attributeHolder as T
        }
    }

    private val res = object: Response() {

    }

    @Test
    fun eventBusShouldUseHandlerAndSendMail(){

        context.expecting {
            oneOf(mockMailer).mail(testUserRegisteredEvent.email,
                    "Welcome to the spark bank",
                    "This was sent via a push pubsub")
            will(returnValue(HttpStatus.OK_200))
        }

        val route = fakeEventBus.register(mapOf(UserRegisteredEvent::class.java to mailHandler))

        assertThat(route.handle(req, res) as Int, Is(HttpStatus.OK_200))
    }
    
}