package com.clouway.bankapp.adapter.spark

import com.google.api.core.ApiFutureCallback
import com.google.api.core.ApiFutures
import com.google.api.gax.rpc.ApiException
import com.google.appengine.api.taskqueue.QueueFactory
import com.google.appengine.api.taskqueue.TaskOptions
import com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl
import com.google.pubsub.v1.ProjectTopicName
import com.google.cloud.ServiceOptions
import com.google.cloud.pubsub.v1.Publisher
import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import org.eclipse.jetty.http.HttpStatus
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class RegisterListener : PropertyChangeListener {
    override fun propertyChange(p0: PropertyChangeEvent) {
        val email = p0.newValue as String

        val projectId = ServiceOptions.getDefaultProjectId()

        val topicId = "register-mailing"

        val topicName = ProjectTopicName.of(projectId, topicId)

        val publisher: Publisher by lazy { Publisher.newBuilder(topicName).build() }

        try{
            val data = ByteString.copyFromUtf8(email)
            val pubsubMessage = PubsubMessage.newBuilder().setData(data).build()
            val future = publisher.publish(pubsubMessage)
            ApiFutures.addCallback(future, object :  ApiFutureCallback<String>{
                override fun onSuccess(result: String?) {
                    println("Success")
                }

                override fun onFailure(t: Throwable?) {
                    if(t is Throwable){
                        val exception = t as ApiException
                        println(exception.statusCode)
                    }
                }
            })
        }finally {
            publisher.shutdown()
        }
    }
}