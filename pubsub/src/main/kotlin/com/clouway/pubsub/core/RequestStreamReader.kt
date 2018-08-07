package com.clouway.pubsub.core

import java.io.InputStream

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
internal interface RequestStreamReader<out T> {
    fun read(requestStreamInput: InputStream): T
}