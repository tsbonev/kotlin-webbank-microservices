package com.clouway.transactionlistener

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import org.hamcrest.CoreMatchers.`is` as Is

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class LaunchConcurrencyTest {

    @Test
    fun updateCounterAsync(){

        val n = 10_000

        val counter = AtomicInteger()

        for (i in 1..n){
            runBlocking {
                launch {
                    counter.addAndGet(1)
                }
            }
        }

        Assert.assertThat(counter.get(), Is(n))
    }
}