package com.clouway.bankapp.core

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.ArrayList
import java.util.EventListener

/**
 * Announcer class is a helper class which is used to announce different kind of message to one or several listeners that
 * are attached to the current announcer.
 *
 * @author Miroslav Genov (mgenov@gmail.com)
 */
class Announcer<T : EventListener> internal constructor(listenerType: Class<out T>) {


    private val proxy: T

    private val listeners = ArrayList<T>()

    init {
        proxy = listenerType.cast(Proxy.newProxyInstance(listenerType.classLoader, arrayOf<Class<*>>(listenerType)) { o, method, objects ->
            announce(method, objects)
            null
        })
    }

    fun addListener(listener: T) {
        listeners.add(listener)
    }

    fun announce(): T {
        return proxy
    }

    @Throws(Throwable::class)
    private fun announce(method: Method, objects: Array<Any>) {
        for (listener in listeners) {
            try {
                method.invoke(listener, *objects)
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                throw e.targetException
            }
        }
    }

    companion object {

        fun <T : EventListener> to(listenerType: Class<out T>): Announcer<T> {
            return Announcer(listenerType)
        }

        fun <T : EventListener> to(listenerType: Class<out T>, listeners: Set<T>): Announcer<T> {
            val announcer = Announcer(listenerType)
            for (listener in listeners) {
                announcer.addListener(listener)
            }
            return announcer
        }
    }
}