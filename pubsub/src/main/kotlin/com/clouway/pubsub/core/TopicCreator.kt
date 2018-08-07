package com.clouway.pubsub.core

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
internal interface TopicCreator {
    fun create(topic: String)
}