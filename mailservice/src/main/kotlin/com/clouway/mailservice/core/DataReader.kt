package com.clouway.mailservice.core

import javax.servlet.http.HttpServletRequest

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface DataReader {
    fun readData(request: HttpServletRequest): Any
}