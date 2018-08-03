package com.clouway.bankapp.adapter.spark

import com.google.appengine.api.taskqueue.QueueFactory
import com.google.appengine.api.taskqueue.TaskOptions
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class RegisterListener : PropertyChangeListener {
    override fun propertyChange(p0: PropertyChangeEvent) {
        val queue = QueueFactory.getQueue("mailing-queue")
        queue.add(TaskOptions.Builder.withUrl("/mail")
                .param("email", p0.newValue as String))
    }
}