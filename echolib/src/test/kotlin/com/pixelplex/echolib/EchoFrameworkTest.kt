package com.pixelplex.echolib

import com.pixelplex.echolib.exception.LocalException
import com.pixelplex.echolib.model.Account
import com.pixelplex.echolib.model.Balance
import com.pixelplex.echolib.model.HistoryResponse
import com.pixelplex.echolib.support.Api
import com.pixelplex.echolib.support.EmptyCallback
import com.pixelplex.echolib.support.Settings
import com.pixelplex.echolib.support.concurrent.future.FutureTask
import com.pixelplex.echolib.support.concurrent.future.wrapResult
import com.pixelplex.echolib.support.fold
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test
import kotlin.concurrent.thread

/**
 * Test cases for [EchoFramework]
 *
 * @author Dmitriy Bushuev
 */
class EchoFrameworkTest {

    private fun initFramework() =
        EchoFramework.create(
            Settings.Configurator()
                .setReturnOnMainThread(false)
                .setApis(
                    Api.DATABASE,
                    Api.NETWORK_BROADCAST,
                    Api.ACCOUNT_HISTORY
                )
                .configure()
        )

    @Test
    fun connectTest() {
        val framework = initFramework()

        assertTrue(connect(framework) ?: false)
    }

    @Test
    fun loginTest() {
        val framework = initFramework()

        val futureLogin =
            FutureTask<Account>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.login("dimaty123", "P5JVzpPDitVodHMoj4zZspn7e8EYiDeoarkCEixS5tD6z",
            object : Callback<Account> {
                override fun onSuccess(result: Account) {
                    futureLogin.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    futureLogin.setComplete(error)
                }

            })

        val account = futureLogin.get()
        assertTrue(account != null)

        val futureLoginFailure =
            FutureTask<Account>()

        framework.login("dimaty123", "WrongPassword",
            object : Callback<Account> {
                override fun onSuccess(result: Account) {
                    futureLoginFailure.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    futureLoginFailure.setComplete(error)
                }

            })

        var accountFail: Account? = null

        futureLoginFailure.wrapResult<Exception, Account>().fold({ foundAccount ->
            accountFail = foundAccount
        }, {
        })

        assertTrue(accountFail == null)
    }

    @Test
    fun getAccountTest() {
        val framework = initFramework()

        val futureAccount =
            FutureTask<Account>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.getAccount("dimaty123", object :
            Callback<Account> {
            override fun onSuccess(result: Account) {
                futureAccount.setComplete(result)
            }

            override fun onError(error: LocalException) {
                futureAccount.setComplete(error)
            }

        })

        val account = futureAccount.get()
        assertTrue(account != null)
    }

    @Test
    fun checkAccountReservedTest() {
        val framework = initFramework()

        val futureCheckReserved =
            FutureTask<Boolean>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.checkAccountReserved("dimaty123", object :
            Callback<Boolean> {
            override fun onSuccess(result: Boolean) {
                futureCheckReserved.setComplete(result)
            }

            override fun onError(error: LocalException) {
                futureCheckReserved.setComplete(error)
            }

        })

        assertTrue(futureCheckReserved.get() ?: false)

        val futureCheckAvailable =
            FutureTask<Boolean>()

        framework.checkAccountReserved("edgewruferjd", object :
            Callback<Boolean> {
            override fun onSuccess(result: Boolean) {
                futureCheckAvailable.setComplete(result)
            }

            override fun onError(error: LocalException) {
                futureCheckAvailable.setComplete(error)
            }

        })

        assertFalse(futureCheckAvailable.get() ?: false)
    }

    @Test
    fun getBalanceTest() {
        val framework = initFramework()

        val futureBalanceExistent =
            FutureTask<Balance>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.getBalance("dimaty123", "1.3.0", object :
            Callback<Balance> {
            override fun onSuccess(result: Balance) {
                futureBalanceExistent.setComplete(result)
            }

            override fun onError(error: LocalException) {
                futureBalanceExistent.setComplete(error)
            }

        })

        assertTrue(futureBalanceExistent.get() != null)

        val futureBalanceNonexistent =
            FutureTask<Balance>()

        framework.getBalance("dimaty123", "ergergger", object :
            Callback<Balance> {
            override fun onSuccess(result: Balance) {
                futureBalanceNonexistent.setComplete(result)
            }

            override fun onError(error: LocalException) {
                futureBalanceNonexistent.setComplete(error)
            }

        })

        var balance: Balance? = null

        futureBalanceNonexistent.wrapResult<Exception, Balance>().fold({ foundBalance ->
            balance = foundBalance
        }, {
        })

        assertTrue(balance == null)
    }

    @Test
    fun accountHistoryTest() {
        val framework = initFramework()

        val futureAccountHistory =
            FutureTask<HistoryResponse>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.getAccountHistory("1.2.23215", "1.11.37878951",
            "1.11.37878741",
            10,
            "1.3.0", object : Callback<HistoryResponse> {
                override fun onSuccess(result: HistoryResponse) {
                    futureAccountHistory.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    futureAccountHistory.setComplete(error)
                }

            })

        assertNotNull(futureAccountHistory.get())
    }

    @Test
    fun changePasswordTest() {
        val framework = initFramework()

        val futureChangePassword =
            FutureTask<Boolean>()

        if (connect(framework) == false) Assert.fail("Connection error")

        changePassword(framework, object : Callback<Any> {
            override fun onSuccess(result: Any) {
                futureChangePassword.setComplete(true)
            }

            override fun onError(error: LocalException) {
                futureChangePassword.setComplete(false)
            }

        })

        assertTrue(futureChangePassword.get() ?: false)
    }

    @Test
    fun subscriptionTest() {
        val framework = initFramework()

        val futureSubscription = FutureTask<Account>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.subscribeOnAccount("1.2.23215", object : AccountListener {

            override fun onChange(updatedAccount: Account) {
                futureSubscription.setComplete(updatedAccount)
            }

        })

        thread {
            Thread.sleep(3000)
            changePassword(framework, EmptyCallback())
        }

        assertNotNull(futureSubscription.get())
    }

    private fun changePassword(framework: EchoFramework, callback: Callback<Any>) {
        framework.changePassword(
            "dimaty123",
            "P5JVzpPDitVodHMoj4zZspn7e8EYiDeoarkCEixS5tD6z",
            "P5JVzpPDitVodHMoj4zZspn7e8EYiDeoarkCEixS5tD6z",
            callback
        )
    }

//    @Test
//    fun transferTest() {
//        val framework = initFramework()
//
//        val futureTransfer = FutureTask<String>()
//
//        if (connect(framework) == false) Assert.fail("Connection error")
//
//        framework.sendTransferOperation(
//            "dimaty123",
//            "P5JVzpPDitVodHMoj4zZspn7e8EYiDeoarkCEixS5tD6z",
//            "dariatest2",
//            "100", "1.3.0", object : Callback<String> {
//                override fun onSuccess(result: String) {
//                    futureTransfer.setComplete(result)
//                }
//
//                override fun onError(error: LocalException) {
//                    futureTransfer.setComplete(error)
//                }
//
//            })
//
//        assertNotNull(futureTransfer.get())
//    }

    private fun connect(framework: EchoFramework): Boolean? {
        val futureConnect = FutureTask<Boolean>()

        framework.start(object : Callback<Any> {
            override fun onSuccess(result: Any) {
                futureConnect.setComplete(true)
            }

            override fun onError(error: LocalException) {
                futureConnect.setComplete(false)
            }

        })

        return futureConnect.get()
    }

}