package com.pixelplex.echolib.support

import org.junit.Assert.*
import org.junit.Test

/**
 * Test cases for [Result]
 *
 * @author Dmitriy Bushuev
 */
class ResultTest {

    @Test
    fun foldTest() {
        val successResult = Result.Value(true)

        successResult.fold({ result ->
            assertTrue(result)
        }, {
            fail()
        })

        val errorResult = IllegalArgumentException()
        val failureResult = Result.Error(errorResult)

        failureResult.fold({
            fail()
        }, { error ->
            assertEquals(errorResult, error)
        })

    }

}
