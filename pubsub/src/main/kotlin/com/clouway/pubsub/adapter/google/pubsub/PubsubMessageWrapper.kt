package com.clouway.pubsub.adapter.google.pubsub

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
internal data class PubsubMessageWrapper(val message: JsonPubsubMessage, val subscription: String)