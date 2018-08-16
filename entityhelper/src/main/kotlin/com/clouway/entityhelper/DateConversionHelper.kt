package com.clouway.entityhelper

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
fun LocalDateTime.toUtilDate(): Date{
    return Date.from(this.toInstant(ZoneOffset.UTC))
}
