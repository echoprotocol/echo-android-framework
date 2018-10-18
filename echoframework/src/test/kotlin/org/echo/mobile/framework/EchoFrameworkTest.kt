package org.echo.mobile.framework

import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.*
import org.echo.mobile.framework.model.contract.ContractInfo
import org.echo.mobile.framework.model.contract.ContractResult
import org.echo.mobile.framework.model.contract.ContractStruct
import org.echo.mobile.framework.model.contract.input.AddressInputValueType
import org.echo.mobile.framework.model.contract.input.InputValue
import org.echo.mobile.framework.model.contract.input.NumberInputValueType
import org.echo.mobile.framework.model.contract.input.StringInputValueType
import org.echo.mobile.framework.model.network.Echodevnet
import org.echo.mobile.framework.service.UpdateListener
import org.echo.mobile.framework.support.Api
import org.echo.mobile.framework.support.EmptyCallback
import org.echo.mobile.framework.support.Settings
import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.echo.mobile.framework.support.concurrent.future.completeCallback
import org.echo.mobile.framework.support.concurrent.future.wrapResult
import org.echo.mobile.framework.support.fold
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
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

    private val legalContractId = "1.16.1"
    private val login = "dariatest2"
    private val password = "P5HyvBoQJQKXmcJw5CAK8UkzwFMLK3DAecniAHH7BM6Ci"
    private val legalHistoryItemId = "1.17.2"
    private val legalAssetId = "1.3.0"
    private val legalContractByteCode =
        "60806040526000805534801561001457600080fd5b5061011480610024" +
                "6000396000f300608060405260043610605c5763ffffffff7c010000000000000000000000000000000000" +
                "00000000000000000000006000350416635b34b966811460615780635b9af12b146075578063a87d942c14" +
                "608a578063f5c5ad831460ae575b600080fd5b348015606c57600080fd5b50607360c0565b005b34801560" +
                "8057600080fd5b50607360043560cb565b348015609557600080fd5b50609c60d6565b6040805191825251" +
                "9081900360200190f35b34801560b957600080fd5b50607360dc565b600080546001019055565b60008054" +
                "9091019055565b60005490565b600080546000190190555600a165627a7a7230582016b3f6673de41336e2" +
                "c5d4b136b4e67bbf43062b6bc47eaef982648cd3b92a9d0029"

    private val illegalContractId = "1.16.-1"
    private val illegalHistoryItemId = "1.17.-1"

    private val contractId = "1.16.16186"
    private val ownerId = "1.2.95"
    private val ownerPassword = "newTestPass"

    @Test
    fun connectTest() {
        val framework = initFramework()

        assertTrue(connect(framework) ?: false)
    }

    @Test(expected = LocalException::class)
    fun disconnectTest() {
        val framework = initFramework()

        assertTrue(connect(framework) ?: false)

        val futureHistory = FutureTask<HistoryResponse>()

        framework.getAccountHistory(
            "1.2.18", "1.11.1",
            "1.11.20000",
            100,
            futureHistory.completeCallback()
        )

        framework.stop()

        futureHistory.get()
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

        val futureLogin = FutureTask<FullAccount>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.isOwnedBy(
            "dima1",
            "P5J8pDyzznMmEdiBCdgB7VKtMBuxw5e4MAJEo3sfUbxcM",
            futureLogin.completeCallback()
        )

        val account = futureLogin.get()
        assertTrue(account != null)

        val futureLoginFailure = FutureTask<FullAccount>()

        framework.isOwnedBy(
            "dima1",
            "WrongPassword",
            futureLoginFailure.completeCallback()
        )

        var accountFail: FullAccount? = null

        futureLoginFailure.wrapResult<Exception, FullAccount>().fold({ foundAccount ->
            accountFail = foundAccount
        }, {
        })

        assertTrue(accountFail == null)
    }

    @Test
    fun getAccountTest() {
        val framework = initFramework()

        val futureAccount = FutureTask<FullAccount>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.getAccount("dima1", futureAccount.completeCallback())

        val account = futureAccount.get()
        assertTrue(account != null)
    }

    @Test
    fun checkAccountReservedTest() {
        val framework = initFramework()

        val futureCheckReserved = FutureTask<Boolean>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.checkAccountReserved("dima2", futureCheckReserved.completeCallback())

        assertTrue(futureCheckReserved.get() ?: false)

        val futureCheckAvailable = FutureTask<Boolean>()

        framework.checkAccountReserved("edgewruferjd", futureCheckAvailable.completeCallback())

        assertFalse(futureCheckAvailable.get() ?: false)
    }

    @Test
    fun getBalanceTest() {
        val framework = initFramework()

        val futureBalanceExistent = FutureTask<Balance>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.getBalance("dima2", "1.3.0", futureBalanceExistent.completeCallback())

        assertTrue(futureBalanceExistent.get() != null)
    }

    @Test(expected = LocalException::class)
    fun getNonexistentAssetBalanceTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureBalanceNonexistent = FutureTask<Balance>()

        framework.getBalance(
            "dima2",
            "ergergger",
            futureBalanceNonexistent.completeCallback()
        )

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

        framework.getAccountHistory(
            nameOrId, "1.11.1",
            "1.11.20000",
            100,
            futureAccountHistory.completeCallback()
        )

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

        val futureSubscriptionById = FutureTask<FullAccount>()
        val futureSubscriptionResult = FutureTask<Boolean>()

        framework.subscribeOnAccount("1.2.18", object : AccountListener {

            override fun onChange(updatedAccount: FullAccount) {
                futureSubscriptionById.setComplete(updatedAccount)
            }

        }, futureSubscriptionResult.completeCallback())

        thread {
            Thread.sleep(3000)
            sendAmount(framework, EmptyCallback())
        }

        assertNotNull(futureSubscriptionById.get())
        assertTrue(futureSubscriptionResult.get() ?: false)
    }

    @Test
    fun subscriptionByNameTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureSubscriptionByName = FutureTask<FullAccount>()
        val futureSubscriptionResult = FutureTask<Boolean>()

        framework.subscribeOnAccount("dima1", object : AccountListener {
            override fun onChange(updatedAccount: FullAccount) {
                futureSubscriptionByName.setComplete(updatedAccount)
            }

        }, futureSubscriptionResult.completeCallback())

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

    private fun sendAmount(framework: EchoFramework, callback: Callback<Boolean>) {
        framework.sendTransferOperation(
            "dima1",
            "P5J8pDyzznMmEdiBCdgB7VKtMBuxw5e4MAJEo3sfUbxcM",
            "dariatest2",
            "1",
            "1.3.0",
            "1.3.0",
            null,
            callback
        )
    }

    @Test
    fun subscribeOnBlockchainDataUpdatesWithAccountChanges() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureSubscriptionBlockchainData = FutureTask<DynamicGlobalProperties>()
        val futureSubscriptionBlockchainDataResult = FutureTask<Boolean>()
        val futureSubscriptionByName = FutureTask<FullAccount>()
        val futureSubscriptionAccountResult = FutureTask<Boolean>()

        framework.subscribeOnBlockchainData(object : UpdateListener<DynamicGlobalProperties> {
            override fun onUpdate(data: DynamicGlobalProperties) {
                futureSubscriptionBlockchainData.setComplete(data)
            }
        }, futureSubscriptionBlockchainDataResult.completeCallback())

        framework.subscribeOnAccount("dima1", object : AccountListener {
            override fun onChange(updatedAccount: FullAccount) {
                futureSubscriptionByName.setComplete(updatedAccount)
            }
        }, futureSubscriptionAccountResult.completeCallback())

        thread {
            Thread.sleep(3000)
            sendAmount(framework, EmptyCallback())

            Thread.sleep(3000)
            futureSubscriptionBlockchainData.cancel()
        }

        assertNotNull(futureSubscriptionBlockchainData.get())
        assertTrue(futureSubscriptionBlockchainDataResult.get() ?: false)

        assertNotNull(futureSubscriptionByName.get())
        assertTrue(futureSubscriptionAccountResult.get() ?: false)
    }

    @Test
    fun subscriptionOnBlockchainDataUpdates() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureSubscriptionBlockchainData = FutureTask<DynamicGlobalProperties>()
        val futureSubscriptionResult = FutureTask<Boolean>()

        framework.subscribeOnBlockchainData(object : UpdateListener<DynamicGlobalProperties> {
            override fun onUpdate(data: DynamicGlobalProperties) {
                futureSubscriptionBlockchainData.setComplete(data)
            }
        }, futureSubscriptionResult.completeCallback())

        thread {
            Thread.sleep(10000)
            futureSubscriptionBlockchainData.cancel()
        }

        assertNotNull(futureSubscriptionBlockchainData.get())
        assertTrue(futureSubscriptionResult.get() ?: false)
    }

    @Test
    fun subscriptionOnBlock() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureSubscriptionBlock = FutureTask<Block>()
        val futureSubscriptionResult = FutureTask<Boolean>()

        framework.subscribeOnBlock(object : UpdateListener<Block> {
            override fun onUpdate(data: Block) {
                futureSubscriptionBlock.setComplete(data)
            }
        }, futureSubscriptionResult.completeCallback())

        thread {
            Thread.sleep(5000)
            futureSubscriptionBlock.cancel()
        }

        assertNotNull(futureSubscriptionBlock.get())
        assertTrue(futureSubscriptionResult.get() ?: false)
    }

//    @Test
//    fun subscriptionOnContractTest() {
//        val framework = initFramework()
//
//        if (connect(framework) == false) Assert.fail("Connection error")
//
//        val futureSubscriptionBlock = FutureTask<Contract>()
//        val futureSubscriptionResult = FutureTask<Boolean>()
//
//        framework.subscribeOnContract("1.16.16244", object : UpdateListener<Contract> {
//            override fun onUpdate(data: Contract) {
//                futureSubscriptionBlock.setComplete(data)
//            }
//        }, futureSubscriptionResult.completeCallback())
//
//        assertNotNull(futureSubscriptionBlock.get())
//        assertTrue(futureSubscriptionResult.get() ?: false)
//    }

    @Test
    fun transferTest() {
        val framework = initFramework()

        val futureTransfer = FutureTask<Boolean>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.sendTransferOperation(
            "dimaty12345",
            "P5JRnzqtPYLxU9ypfndHczCqt178nzomv4DuspTPr1iTf",
            "dima1",
            "1", "1.3.0", "1.3.0", "Memasik",
            futureTransfer.completeCallback()
        )

        assertTrue(futureTransfer.get() ?: false)
    }

    @Test
    fun getRequiredFeeTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureFee = FutureTask<String>()

        framework.getFeeForTransferOperation(
            "dima1",
            "P5J8pDyzznMmEdiBCdgB7VKtMBuxw5e4MAJEo3sfUbxcM",
            "dima2",
            "10000",
            "1.3.0",
            "1.3.0",
            "message",
            futureFee.completeCallback()
        )

        assertNotNull(futureFee.get())
    }

    @Test(expected = LocalException::class)
    fun getRequiredFeeFailureTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureFee = FutureTask<String>()

        framework.getFeeForTransferOperation(
            "dimaty123",
            "P5J8pDyzznMmEdiBCdgB7VKtMBuxw5e4MAJEo3sfUbxcM",
            "dima2",
            "10000",
            "1.3.1234",
            "1.3.1234",
            "message",
            futureFee.completeCallback()
        )

        assertNotNull(futureFee.get())
    }

    @Test
    fun listAssetsTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureAssets = FutureTask<List<Asset>>()

        framework.listAssets(
            "ECHO", 10,
            futureAssets.completeCallback()
        )

        assertNotNull(futureAssets.get()?.isNotEmpty() ?: false)
    }

    @Test
    fun getAssetsTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureAssets = FutureTask<List<Asset>>()

        framework.getAssets(
            listOf("1.3.0", "1.3.2"),
            futureAssets.completeCallback()
        )

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
//        val asset = Asset("").apply {
//            symbol = "QWEASDGHRTYUIO"
//            precision = 4
//            issuer = Account("1.2.188")
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
//                UnsignedLong.valueOf(100000),
//                0.toLong(),
//                UnsignedLong.ZERO,
//                AssetOptions.ALLOW_COMITEE_PROVIDE_FEEDS,
//                AssetOptions.ALLOW_COMITEE_PROVIDE_FEEDS,
//                Price().apply {
//                    this.quote = AssetAmount(UnsignedLong.valueOf(1), Asset("1.3.1"))
//                    this.base = AssetAmount(UnsignedLong.valueOf(1), Asset("1.3.0"))
//                },
//                "description"
//            )
//
//        asset.assetOptions = options
//
//        framework.createAsset(
//            "dimaty12345", "P5JRnzqtPYLxU9ypfndHczCqt178nzomv4DuspTPr1iTf",
//            asset,
//            futureAsset.completeCallback()
//        )
//
//        assertNotNull(futureAsset.get())
//    }

    @Test
    fun issueAssetTest() {
        val framework = initFramework()

        val futureIssue = FutureTask<Boolean>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.issueAsset(
            "dimaty12345",
            "P5JRnzqtPYLxU9ypfndHczCqt178nzomv4DuspTPr1iTf",
            "1.3.79",
            "1",
            "dima1", "message",
            futureIssue.completeCallback()
        )

        assertTrue(futureIssue.get() ?: false)
    }

//    @Test
//    fun createContractTest() {
//        val framework = initFramework()
//
//        if (connect(framework) == false) Assert.fail("Connection error")
//
//        val future = FutureTask<Boolean>()
//
//        framework.createContract(
//            login,
//            password,
//            legalAssetId,
//            legalContractByteCode,
//            object : Callback<Boolean> {
//                override fun onSuccess(result: Boolean) {
//                    future.setComplete(result)
//                }
//
//                override fun onError(error: LocalException) {
//                    future.setComplete(error)
//                }
//            }
//        )
//
//        assertTrue(future.get() ?: false)
//    }

    @Test
    fun callContractTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<Boolean>()

        framework.callContract(
            login,
            password,
            legalAssetId,
            legalContractId,
            "incrementCounter",
            listOf(),
            future.completeCallback()
        )

        assertTrue(future.get() ?: false)
    }

    @Test
    fun callContractWithAddressParameterTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<Boolean>()

        val address = "1.2.18".split(".").last()

        framework.callContract(
            ownerId, ownerPassword, "1.3.0", contractId, "addUserToDoor",
            listOf(
                InputValue(NumberInputValueType("int64"), "3"),
                InputValue(
                    AddressInputValueType(),
                    address
                )
            ),
            future.completeCallback()
        )

        assertTrue(future.get() ?: false)
    }

    @Test
    fun callContractWithStringParameterTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<Boolean>()

        framework.callContract(
            ownerId, ownerPassword, "1.3.0", contractId, "addDoor",
            listOf(
                InputValue(NumberInputValueType("int64"), "3"),
                InputValue(
                    StringInputValueType(),
                    "door + ${SimpleDateFormat("HH:mm").format(System.currentTimeMillis())}"
                )
            ),
            future.completeCallback()
        )

        assertTrue(future.get() ?: false)
    }

    @Test(expected = LocalException::class)
    fun callContractFailureTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<Boolean>()

        framework.callContract(
            login,
            password,
            legalAssetId,
            illegalContractId,
            "incrementCounter",
            listOf(),
            future.completeCallback()
        )

        assertFalse(future.get() ?: false)
    }

    @Test
    fun queryContractTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<String>()

        framework.queryContract(
            login,
            legalAssetId,
            legalContractId,
            "getCount",
            listOf(),
            future.completeCallback()
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
            login,
            legalAssetId,
            illegalContractId,
            "getCounter()",
            listOf(),
            future.completeCallback()
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
            legalHistoryItemId,
            future.completeCallback()
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
            illegalHistoryItemId,
            future.completeCallback()
        )

        assertNotNull(future.get())
    }

    @Test
    fun getAllContractsTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<List<ContractInfo>>()

        framework.getAllContracts(
            future.completeCallback()
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
            listOf(legalContractId),
            future.completeCallback()
        )

        val contractResult = future.get()
        assertNotNull(contractResult)
        assert(contractResult!!.isNotEmpty())
        assert(contractResult.size == 1)
        assertNotNull(contractResult.first())
    }

    @Test
    fun getContractsFailureTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<List<ContractInfo>>()

        framework.getContracts(
            listOf(illegalContractId),
            future.completeCallback()
        )

        assertNull(future.get()?.first())
    }

    @Test
    fun getContractTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<ContractStruct>()

        framework.getContract(
            legalContractId,
            future.completeCallback()
        )

        val contractResult = future.get()
        assertNotNull(contractResult)
        assertEquals(contractResult!!.contractInfo.getObjectId(), legalContractId)
    }

    @Test(expected = LocalException::class)
    fun getContractFailureTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<ContractStruct>()

        framework.getContract(
            illegalContractId,
            future.completeCallback()
        )

        assertNotNull(future.get())
    }

    @Test
    fun getBlockDataTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<BlockData>()

        framework.databaseApiService.getBlockData(future.completeCallback())

        assertNotNull(future.get())
    }

    @Test
    fun getDynamicGlobalPropertiesTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<DynamicGlobalProperties>()

        framework.databaseApiService.getDynamicGlobalProperties(future.completeCallback())

        assertNotNull(future.get())
    }

    @Test
    fun getBlockTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<Block>()

        framework.databaseApiService.getBlock("1", future.completeCallback())

        assertNotNull(future.get())
    }

    @Test
    fun getChainIdTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<String>()

        framework.databaseApiService.getChainId(future.completeCallback())

        assertNotNull(future.get())
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
