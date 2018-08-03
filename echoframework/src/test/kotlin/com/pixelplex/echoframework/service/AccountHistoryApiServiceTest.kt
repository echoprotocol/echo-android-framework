package com.pixelplex.echoframework.service

import com.google.common.primitives.UnsignedLong
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.model.*
import com.pixelplex.echoframework.model.network.Echodevnet
import com.pixelplex.echoframework.model.operations.TransferOperation
import com.pixelplex.echoframework.service.internal.AccountHistoryApiServiceImpl
import com.pixelplex.echoframework.support.error
import com.pixelplex.echoframework.support.value
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * Test cases for [AccountHistoryApiService]
 *
 * @author
 */
class AccountHistoryApiServiceTest {

    private lateinit var response: HistoryResponse

    @Before
    fun setUp() {
        response = HistoryResponse(
            listOf(
                HistoricalTransfer(
                    "testId", TransferOperation(
                        Account("1.2.18"), Account("1.2.119"), AssetAmount(
                            UnsignedLong.valueOf(1), Asset("1.3.0")
                        )
                    ), Date()
                )
            )
        )
    }

    @Test
    fun getAccountHistoryTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(response)

        val accountHistoryApiService =
            AccountHistoryApiServiceImpl(socketCoreComponent, Echodevnet())

        val historyCallback = object : Callback<HistoryResponse> {

            override fun onSuccess(result: HistoryResponse) {
                Assert.assertFalse(result.transactions.isEmpty())
                Assert.assertTrue(result.transactions.size == 1)
            }

            override fun onError(error: LocalException) {
                Assert.fail()
            }

        }

        getHistory(accountHistoryApiService, historyCallback)
    }

    @Test
    fun getAccountHistoryErrorTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(null)

        val accountHistoryApiService =
            AccountHistoryApiServiceImpl(socketCoreComponent, Echodevnet())

        val historyCallback = object : Callback<HistoryResponse> {

            override fun onSuccess(result: HistoryResponse) {
                Assert.fail()
            }

            override fun onError(error: LocalException) {
                Assert.assertNotNull(error)
            }

        }

        getHistory(accountHistoryApiService, historyCallback)
    }

    private fun getHistory(
        historyService: AccountHistoryApiService,
        historyCallback: Callback<HistoryResponse>
    ) = historyService.getAccountHistory(
        "1.2.18",
        "1.11.1",
        "1.11.11",
        10,
        historyCallback
    )


    @Test
    fun getAccountHistoryResultTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(response)

        val accountHistoryApiService =
            AccountHistoryApiServiceImpl(socketCoreComponent, Echodevnet())

        accountHistoryApiService.getAccountHistory(
            "1.2.18",
            "1.11.1",
            "1.11.11",
            10
        )
            .value { history ->
                Assert.assertFalse(history.transactions.isEmpty())
                Assert.assertTrue(history.transactions.size == 1)
            }
            .error { Assert.fail() }
    }

}
