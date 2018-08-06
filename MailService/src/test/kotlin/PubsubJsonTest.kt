import com.google.appengine.repackaged.com.google.gson.Gson
import com.google.pubsub.v1.PubsubMessage
import org.apache.geronimo.mail.util.Base64
import org.junit.Test

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class PubsubJsonTest {

    val json = """
        {
  "message": {
    "attributes": {
      "key": "value"
    },
    "data": "SGVsbG8gQ2xvdWQgUHViL1N1YiEgSGVyZSBpcyBteSBtZXNzYWdlIQ==",
    "message_id": "136969346945"
    },
    "subscription": "projects/myproject/subscriptions/mysubscription"
    }
    """.trimIndent()

    data class PubsubMessageWrapper(val attributes: Any, val data: String, val message_id: String)
    data class PubsubCustomWrapper(val message: PubsubMessageWrapper, val subscription: String)
    data class PubsubWrapper(val message: PubsubMessage, val subscription: String)


    @Test
    fun convertToPubsubMessage(){
        val wholeMessage = Gson().fromJson(json, PubsubMessage::class.java)
        val wrappedMessage = Gson().fromJson(json, PubsubWrapper::class.java)
        val customMessage = Gson().fromJson(json, PubsubCustomWrapper::class.java)

        val decodedData = String(Base64.decode(customMessage.message.data))

        println(wholeMessage)
        println(wrappedMessage)
        println(customMessage.message.data)
        println(decodedData)
    }

}