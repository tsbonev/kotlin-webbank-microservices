package adapter.spark

import com.clouway.mailservice.adapter.gae.PubsubDataReader
import com.clouway.mailservice.adapter.spark.MailController
import com.clouway.mailservice.adapter.spark.PubsubController
import com.clouway.mailservice.core.DataReader
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
import java.io.InputStream
import javax.servlet.ReadListener
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest

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
    
    private val mockDataReader = context.mock(DataReader::class.java)
    private val mockMailer = context.mock(Mailer::class.java)
    private val mockRequest = context.mock(HttpServletRequest::class.java)

    private val mailController = MailController(mailer = mockMailer)
    private val pubsubController = PubsubController(mailController, mockDataReader)

    private val messageJson = """
    {
    "message": {
        "attributes": {
            "key": "value"
            },
        "data": "dHNib25ldkBnbWFpbC5jb20=",
        "message_id": "136969346945"
        },
    "subscription": "projects/myproject/subscriptions/mysubscription"
    }
    """.trimIndent()

    private val messageStream: InputStream = messageJson.byteInputStream()

    private val servletInputStream = object: ServletInputStream(){

        override fun setReadListener(readListener: ReadListener?) {
            TODO("not implemented")
        }

        override fun isFinished(): Boolean {
            TODO("not implemented")
        }

        override fun isReady(): Boolean {
            TODO("not implemented")
        }

        override fun read(): Int {
            return messageStream.read()
        }
    }

    private var attributeHolder: String = ""

    private val req = object: Request(){
        override fun raw(): HttpServletRequest {
            return mockRequest
        }

        override fun attribute(attribute: String?, value: Any?) {
            attributeHolder = value!! as String
        }

        override fun <T : Any?> attribute(attribute: String?): T {
            return attributeHolder as T
        }
    }

    private val res = object: Response() {

    }

    @Test
    fun shouldSendMail(){

        context.expecting {
            oneOf(mockDataReader).readData(mockRequest)
            will(returnValue("tsbonev@gmail.com"))
            oneOf(mockMailer).mail("tsbonev@gmail.com",
                    "Welcome to the spark bank",
                    "This was sent via a push pubsub")
            will(returnValue(HttpStatus.OK_200))
        }

        assertThat(pubsubController.handle(req, res) == HttpStatus.OK_200, Is(true))
    }

    @Test
    fun shouldDecodeDataAndSendMail(){

        val dataReader = PubsubDataReader()

        context.expecting {
            oneOf(mockRequest).inputStream
            will(returnValue(servletInputStream))
            oneOf(mockMailer).mail("tsbonev@gmail.com",
                    "Welcome to the spark bank",
                    "This was sent via a push pubsub")
            will(returnValue(HttpStatus.OK_200))
        }

        val pubsubController = PubsubController(mailController, dataReader)
        assertThat(pubsubController.handle(req, res) == HttpStatus.OK_200, Is(true))
    }
}