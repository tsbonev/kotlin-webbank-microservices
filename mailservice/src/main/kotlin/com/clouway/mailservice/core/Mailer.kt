package com.clouway.mailservice.core

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface Mailer {
    fun mail(receiver: String, title: String, content: String): Int
}