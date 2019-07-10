package org.echo.mobile.framework.service

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.HistoricalTransfer
import org.echo.mobile.framework.model.HistoryResponse
import org.echo.mobile.framework.model.network.Echodevnet
import org.echo.mobile.framework.model.operations.TransferOperation
import org.echo.mobile.framework.service.internal.AccountHistoryApiServiceImpl
import org.echo.mobile.framework.support.error
import org.echo.mobile.framework.support.value
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.Date

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
                            UnsignedLong.ONE, Asset("1.3.0")
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
