package com.clouway.mailservice.core

/**
 * Sends an email to a receiver with
 * a title and content.
 *
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface Mailer {

    /**
     * Sends an email.
     *
     * @param receiver Receiver of the email
     * @param title Title of the email
     * @param content Content of the email
     * @return A status code
     */
    fun mail(receiver: String, title: String, content: String): Int
}