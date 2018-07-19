package com.pixelplex.echolib.support

import org.junit.Assert
import org.junit.Test

/**
 * Test cases for [Api]
 *
 * @author Dmitriy Bushuev
 */
class ApiTest {

    @Test
    fun uninitializedIdTest() {
        Assert.assertTrue(Api.NETWORK_BROADCAST.getId() == -1)
    }

    @Test
    fun initializedIdTest() {
        val historyId = 2
        val databaseId = 1234

        Api.ACCOUNT_HISTORY.updateId(historyId)
        Api.DATABASE.updateId(databaseId)

        Assert.assertTrue(Api.ACCOUNT_HISTORY.getId() == historyId)
        Assert.assertTrue(Api.DATABASE.getId() == databaseId)
        Assert.assertTrue(Api.CRYPTO.getId() == -1)

    }

}