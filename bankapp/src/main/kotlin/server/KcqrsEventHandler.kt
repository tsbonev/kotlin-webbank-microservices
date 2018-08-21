package server

import com.clouway.kcqrs.adapter.appengine.AbstractEventHandlerServlet
import com.clouway.kcqrs.core.Event
import com.clouway.kcqrs.core.MessageBus
import com.google.appengine.repackaged.com.google.gson.Gson
import java.io.InputStream
import java.io.InputStreamReader

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class KcqrsEventHandler : AbstractEventHandlerServlet() {
    private val gson = Gson()

    override fun decode(inputStream: InputStream, type: Class<*>): Event {
        val event = gson.fromJson(InputStreamReader(inputStream, "UTF-8"), type)
        return event as Event
    }

    override fun messageBus(): MessageBus {
        return CQRSContext.messageBus()
    }

}