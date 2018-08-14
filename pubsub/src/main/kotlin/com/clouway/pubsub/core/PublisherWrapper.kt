package com.clouway.pubsub.core

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
internal interface PublisherWrapper<in P, out T> {
    fun publish(message: P)
    fun shutdown()
    fun raw(): T
}