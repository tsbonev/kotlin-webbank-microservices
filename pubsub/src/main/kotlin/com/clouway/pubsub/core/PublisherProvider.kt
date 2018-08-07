package com.clouway.pubsub.core

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
internal interface PublisherProvider<in P, out T> {
    fun get(topic: String): PublisherWrapper<P, T>
}