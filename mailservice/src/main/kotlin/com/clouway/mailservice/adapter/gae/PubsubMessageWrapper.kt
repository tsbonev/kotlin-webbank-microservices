package com.clouway.mailservice.adapter.gae

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
data class PubsubMessageWrapper(val message: PubsubMessage, val subscription: String)