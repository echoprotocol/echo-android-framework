package com.pixelplex.echolib.support

import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * Test cases for [Result]
 *
 * @author Dmitriy Bushuev
 */
class ResultTest {

    @Test
    fun foldTest() {
        val successResult = Success<Boolean, Exception>(true)

        successResult.fold({ result ->
            assertTrue(result)
        }, {
            fail()
        })

        val failureResult = Failure<Boolean, Exception>(IllegalArgumentException())

        failureResult.fold({
            fail()
        }, { error ->
            assertTrue(error is IllegalArgumentException)
        })

    }

}
