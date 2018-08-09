package com.pixelplex.echoframework

import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.model.Account
import com.pixelplex.echoframework.model.Asset
import com.pixelplex.echoframework.model.Balance
import com.pixelplex.echoframework.model.HistoryResponse
import com.pixelplex.echoframework.model.contract.ContractInfo
import com.pixelplex.echoframework.model.contract.ContractResult
import com.pixelplex.echoframework.model.contract.ContractStruct
import com.pixelplex.echoframework.model.network.Echodevnet
import com.pixelplex.echoframework.support.Api
import com.pixelplex.echoframework.support.EmptyCallback
import com.pixelplex.echoframework.support.Settings
import com.pixelplex.echoframework.support.concurrent.future.FutureTask
import com.pixelplex.echoframework.support.concurrent.future.wrapResult
import com.pixelplex.echoframework.support.fold
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
                .setUrl(ECHO_URL)
                .setNetworkType(Echodevnet())
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
    fun connectFailedTest() {
        val framework = EchoFramework.create(
            Settings.Configurator()
                .setUrl("wrongUrl")
                .setReturnOnMainThread(false)
                .setApis(
                    Api.DATABASE,
                    Api.NETWORK_BROADCAST,
                    Api.ACCOUNT_HISTORY
                )
                .configure()
        )

        assertFalse(connect(framework) ?: false)
    }

    @Test
    fun isOwnedByTest() {
        val framework = initFramework()

        val futureLogin = FutureTask<Account>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.isOwnedBy("dima1", "P5J8pDyzznMmEdiBCdgB7VKtMBuxw5e4MAJEo3sfUbxcM",
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

        val futureLoginFailure = FutureTask<Account>()

        framework.isOwnedBy("dima1", "WrongPassword",
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

        val futureAccount = FutureTask<Account>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.getAccount("dima1", object :
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

        val futureCheckReserved = FutureTask<Boolean>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.checkAccountReserved("dima2", object :
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

        val futureBalanceExistent = FutureTask<Balance>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.getBalance("dima2", "1.3.0", object :
            Callback<Balance> {
            override fun onSuccess(result: Balance) {
                futureBalanceExistent.setComplete(result)
            }

            override fun onError(error: LocalException) {
                futureBalanceExistent.setComplete(error)
            }

        })

        assertTrue(futureBalanceExistent.get() != null)
    }

    @Test(expected = LocalException::class)
    fun getNonexistentAssetBalanceTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureBalanceNonexistent = FutureTask<Balance>()

        framework.getBalance("dima2", "ergergger", object :
            Callback<Balance> {
            override fun onSuccess(result: Balance) {
                futureBalanceNonexistent.setComplete(result)
            }

            override fun onError(error: LocalException) {
                futureBalanceNonexistent.setComplete(error)
            }

        })

        assertNotNull(futureBalanceNonexistent.get())
    }

    @Test
    fun accountHistoryByIdTest() = getAccountHistory("1.2.18")

    @Test
    fun accountHistoryByNameTest() = getAccountHistory("dima1")

    private fun getAccountHistory(nameOrId: String) {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureAccountHistory = FutureTask<HistoryResponse>()

        framework.getAccountHistory(nameOrId, "1.11.1",
            "1.11.20000",
            100,
            "1.3.0", object : Callback<HistoryResponse> {
                override fun onSuccess(result: HistoryResponse) {
                    futureAccountHistory.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    futureAccountHistory.setComplete(error)
                }

            })

        val history = futureAccountHistory.get()

        assertNotNull(history)
        assertTrue(history?.transactions?.isNotEmpty() ?: false)
        assertNotNull(history?.transactions?.get(0)?.timestamp)
    }

    @Test
    fun changePasswordTest() {
        val framework = initFramework()

        val futureChangePassword = FutureTask<Boolean>()

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
    fun subscriptionByIdTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureSubscriptionById = FutureTask<Account>()
        val futureSubscriptionResult = FutureTask<Boolean>()

        framework.subscribeOnAccount("1.2.18", object : AccountListener {

            override fun onChange(updatedAccount: Account) {
                futureSubscriptionById.setComplete(updatedAccount)
            }

        }, object : Callback<Boolean>{
            override fun onSuccess(result: Boolean) {
                futureSubscriptionResult.setComplete(result)
            }

            override fun onError(error: LocalException) {
                futureSubscriptionResult.setComplete(error)
            }
        })

        thread {
            Thread.sleep(3000)
            changePassword(framework, EmptyCallback())
        }

        assertNotNull(futureSubscriptionById.get())
        assertTrue(futureSubscriptionResult.get() ?: false)
    }

    @Test
    fun subscriptionByNameTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureSubscriptionByName = FutureTask<Account>()
        val futureSubscriptionResult = FutureTask<Boolean>()

        framework.subscribeOnAccount("dima1", object : AccountListener {

            override fun onChange(updatedAccount: Account) {
                futureSubscriptionByName.setComplete(updatedAccount)
            }

        }, object : Callback<Boolean> {
            override fun onSuccess(result: Boolean) {
                futureSubscriptionResult.setComplete(result)
            }

            override fun onError(error: LocalException) {
                futureSubscriptionResult.setComplete(error)
            }
        })

        thread {
            Thread.sleep(3000)
            changePassword(framework, EmptyCallback())
        }

        assertNotNull(futureSubscriptionByName.get())
        assertTrue(futureSubscriptionResult.get() ?: false)
    }

    private fun changePassword(framework: EchoFramework, callback: Callback<Any>) {
        framework.changePassword(
            "dima1",
            "P5J8pDyzznMmEdiBCdgB7VKtMBuxw5e4MAJEo3sfUbxcM",
            "P5J8pDyzznMmEdiBCdgB7VKtMBuxw5e4MAJEo3sfUbxcM",
            callback
        )
    }

    @Test
    fun transferTest() {
        val framework = initFramework()

        val futureTransfer = FutureTask<Boolean>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.sendTransferOperation(
            "dima1",
            "P5J8pDyzznMmEdiBCdgB7VKtMBuxw5e4MAJEo3sfUbxcM",
            "dima2",
            "1", "1.3.0", "Memasik", object : Callback<Boolean> {
                override fun onSuccess(result: Boolean) {
                    futureTransfer.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    futureTransfer.setComplete(error)
                }

            })

        assertTrue(futureTransfer.get() ?: false)
    }

    @Test
    fun getRequiredFeeTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureFee = FutureTask<String>()

        framework.getFeeForTransferOperation(
            "dima1",
            "dima2",
            "10000",
            "1.3.0",
            object : Callback<String> {
                override fun onSuccess(result: String) {
                    futureFee.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    futureFee.setComplete(error)
                }

            })

        assertNotNull(futureFee.get())
    }

    @Test(expected = LocalException::class)
    fun getRequiredFeeFailureTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureFee = FutureTask<String>()

        framework.getFeeForTransferOperation(
            "dimaty123",
            "dima2",
            "10000",
            "1.3.1234",
            object : Callback<String> {
                override fun onSuccess(result: String) {
                    futureFee.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    futureFee.setComplete(error)
                }

            })

        assertNotNull(futureFee.get())
    }

    @Test
    fun listAssetsTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureAssets = FutureTask<List<Asset>>()

        framework.listAssets(
            "ECHO", 10,
            object : Callback<List<Asset>> {
                override fun onSuccess(result: List<Asset>) {
                    futureAssets.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    futureAssets.setComplete(error)
                }

            })

        assertNotNull(futureAssets.get()?.isNotEmpty() ?: false)
    }

    @Test
    fun getAssetsTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureAssets = FutureTask<List<Asset>>()

        framework.getAssets(
            listOf("1.3.0", "1.3.2"),
            object : Callback<List<Asset>> {
                override fun onSuccess(result: List<Asset>) {
                    futureAssets.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    futureAssets.setComplete(error)
                }

            })

        assertNotNull(futureAssets.get()?.isNotEmpty() ?: false)
    }

//    @Test
//    fun createAssetTest() {
//        val framework = initFramework()
//
//        if (connect(framework) == false) Assert.fail("Connection error")
//
//        val futureAsset = FutureTask<Boolean>()
//
//        val asset = Asset("", "RTJHRTDFS", 4, "1.2.18").apply {
//            setBtsOptions(
//                BitassetOptions(
//                    86400, 7, 86400,
//                    100, 2000
//                )
//            )
//
//            predictionMarket = false
//        }
//
//        val options =
//            AssetOptions(
//                UnsignedLong.valueOf(100000), 0.toLong(), UnsignedLong.ZERO, 79, 1,
//                Price().apply {
//                    this.quote = AssetAmount(UnsignedLong.valueOf(1), Asset("1.3.1"))
//                    this.base = AssetAmount(UnsignedLong.valueOf(1), Asset("1.3.0"))
//                }, "description"
//            )
//
//        asset.assetOptions = options
//
//        framework.createAsset(
//            "dima1", "P5KctJPedZ4K9T77KgmqhVNJ5H6FojEN6fRVp9PYyhAb9",
//            asset,
//            object : Callback<Boolean> {
//                override fun onSuccess(result: Boolean) {
//                    futureAsset.setComplete(result)
//                }
//
//                override fun onError(error: LocalException) {
//                    futureAsset.setComplete(error)
//                }
//
//            })
//
//        assertNotNull(futureAsset.get())
//    }

    @Test
    fun issueAssetTest() {
        val framework = initFramework()

        val futureIssue = FutureTask<Boolean>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.issueAsset(
            "dima2",
            "P5KdaL4usZTknpaBwpi2xVAEPJxtvPRopDY1vG6BJTbr5S3ZLksx",
            "1.3.1",
            "1",
            "dima1", "message",
            object : Callback<Boolean> {
                override fun onSuccess(result: Boolean) {
                    futureIssue.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    futureIssue.setComplete(error)
                }

            })

        assertTrue(futureIssue.get() ?: false)
    }

    @Test
    fun callContractTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<Boolean>()

        framework.callContract(
            "dariatest2",
            "P5HyvBoQJQKXmcJw5CAK8UkzwFMLK3DAecniAHH7BM6Ci",
            "1.3.0",
            "1.16.33",
            "incrementCounter",
            listOf(),
            object : Callback<Boolean> {
                override fun onSuccess(result: Boolean) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )

        assertTrue(future.get() ?: false)
    }

    @Test
    fun queryContractTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<String>()

        framework.queryContract(
            "dariatest2",
            "1.3.0",
            "1.16.33",
            "getCount",
            listOf(),
            object : Callback<String> {
                override fun onSuccess(result: String) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )

        assertNotNull(future.get())
        assert(future.get()!!.isNotEmpty())
    }

    @Test(expected = LocalException::class)
    fun queryContractFailureTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<String>()

        framework.queryContract(
            "dariatest2",
            "1.3.0",
            "1.16.-3",
            "getCounter()",
            listOf(),
            object : Callback<String> {
                override fun onSuccess(result: String) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )

        assertNotNull(future.get())
        assert(future.get()!!.isEmpty())
    }

    @Test
    fun getContractResultTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<ContractResult>()

        framework.getContractResult(
            "1.17.178",
            object : Callback<ContractResult> {
                override fun onSuccess(result: ContractResult) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )

        val contractResult = future.get()
        assertNotNull(contractResult)
        assertEquals(contractResult!!.execRes.excepted, "None")
    }

    @Test(expected = LocalException::class)
    fun getContractResultFailureTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<ContractResult>()

        framework.getContractResult(
            "1.17.-1",
            object : Callback<ContractResult> {
                override fun onSuccess(result: ContractResult) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )

        assertNotNull(future.get())
    }

    @Test
    fun getAllContractsTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<List<ContractInfo>>()

        framework.getAllContracts(
            object : Callback<List<ContractInfo>> {
                override fun onSuccess(result: List<ContractInfo>) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )

        val contractResult = future.get()
        assertNotNull(contractResult)
        assert(contractResult!!.isNotEmpty())
    }

    @Test
    fun getContractsTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<List<ContractInfo>>()

        framework.getContracts(
            listOf("1.16.33", "1.16.62"),
            object : Callback<List<ContractInfo>> {
                override fun onSuccess(result: List<ContractInfo>) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )

        val contractResult = future.get()
        assertNotNull(contractResult)
        assert(contractResult!!.isNotEmpty())
        assert(contractResult.size == 2)
    }

    @Test
    fun getContractTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<ContractStruct>()

        framework.getContract(
            "1.16.33",
            object : Callback<ContractStruct> {
                override fun onSuccess(result: ContractStruct) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )

        val contractResult = future.get()
        assertNotNull(contractResult)
        assertEquals(contractResult!!.contractInfo.getObjectId(), "1.16.33")
    }

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
