package com.clouway.bankapp.core

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
fun LocalDateTime.toUtilDate(): Date{
    return Date.from(this.atZone(ZoneId.systemDefault()).toInstant())
}
