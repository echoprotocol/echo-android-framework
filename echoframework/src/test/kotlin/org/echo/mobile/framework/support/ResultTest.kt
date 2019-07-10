package org.echo.mobile.framework.support

import org.junit.Assert.assertEquals
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

    @Test
    fun map() {
        val success = "success".toValue()

        val v1 = success.map { it.count() }

        assertEquals((v1 as Result.Value<Int>).value, 7)
    }

    @Test
    fun flatMap() {
        val success = "success".toValue()

        val v1 = success.flatMap { it.last().toValue() }

        assertEquals((v1 as Result.Value<Char>).value, 's')
    }

}
