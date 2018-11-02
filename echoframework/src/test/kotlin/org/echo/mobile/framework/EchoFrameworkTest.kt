package org.echo.mobile.framework

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.*
import org.echo.mobile.framework.model.contract.ContractInfo
import org.echo.mobile.framework.model.contract.ContractResult
import org.echo.mobile.framework.model.contract.ContractStruct
import org.echo.mobile.framework.model.contract.input.*
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
import java.lang.Thread.sleep
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

    private val legalContractParamsBytecode =
        "608060405234801561001057600080fd5b50610556806100206000396000f3006080604052600436106100" +
                "57576000357c0100000000000000000000000000000000000000000000000000000000900463ff" +
                "ffffff168063213c1fcb1461005c578063f2c298be14610118578063f8a8fd6d14610181575b60" +
                "0080fd5b34801561006857600080fd5b5061009d600480360381019080803573ffffffffffffff" +
                "ffffffffffffffffffffffffff169060200190929190505050610211565b604051808060200182" +
                "8103825283818151815260200191508051906020019080838360005b838110156100dd57808201" +
                "51818401526020810190506100c2565b50505050905090810190601f16801561010a5780820380" +
                "516001836020036101000a031916815260200191505b509250505060405180910390f35b348015" +
                "61012457600080fd5b5061017f6004803603810190808035906020019082018035906020019080" +
                "80601f016020809104026020016040519081016040528093929190818152602001838380828437" +
                "82019150505050505091929192905050506102c1565b005b34801561018d57600080fd5b506101" +
                "966103e7565b604051808060200182810382528381815181526020019150805190602001908083" +
                "8360005b838110156101d65780820151818401526020810190506101bb565b50505050905090810" +
                "190601f1680156102035780820380516001836020036101000a031916815260200191505b509250" +
                "505060405180910390f35b600060205280600052604060002060009150905080546001816001161" +
                "56101000203166002900480601f0160208091040260200160405190810160405280929190818152" +
                "602001828054600181600116156101000203166002900480156102b95780601f1061028e576101" +
                "008083540402835291602001916102b9565b820191906000526020600020905b81548152906001" +
                "019060200180831161029c57829003601f168201915b505050505081565b806000803373ffffff" +
                "ffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1" +
                "681526020019081526020016000209080519060200190610313929190610485565b507fbec1c4e" +
                "1c777d57c8686d8eff4ba1ec5f8207b289e8d10f9b4be199c6baf00bd3382604051808373fffff" +
                "fffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff" +
                "16815260200180602001828103825283818151815260200191508051906020019080838360005b" +
                "838110156103a957808201518184015260208101905061038e565b50505050905090810190601f1" +
                "680156103d65780820380516001836020036101000a031916815260200191505b50935050505060" +
                "405180910390a150565b60018054600181600116156101000203166002900480601f01602080910" +
                "40260200160405190810160405280929190818152602001828054600181600116156101000203166" +
                "0029004801561047d5780601f106104525761010080835404028352916020019161047d565b8201" +
                "91906000526020600020905b81548152906001019060200180831161046057829003601f16820191" +
                "5b505050505081565b82805460018160011615610100020316600290049060005260206000209060" +
                "1f016020900481019282601f106104c657805160ff19168380011785556104f4565b828001600101" +
                "855582156104f4579182015b828111156104f35782518255916020019190600101906104d8565b5b" +
                "5090506105019190610505565b5090565b61052791905b8082111561052357600081600090555060" +
                "010161050b565b5090565b905600a165627a7a72305820c3d8fad64386213f092995be2224f17d" +
                "efe50984ba08d2e54d9229ec7a2504be0029"


    private val legalContractWithParamsBytecode =
        "608060405233600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908" +
                "373ffffffffffffffffffffffffffffffffffffffff16021790555034801561005157600080fd5" +
                "b50604051602080610375833981018060405281019080805190602001909291905050508060026" +
                "0006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373fffffff" +
                "fffffffffffffffffffffffffffffffff160217905550806000806101000a81548173fffffffff" +
                "fffffffffffffffffffffffffffffff021916908373fffffffffffffffffffffffffffffffffff" +
                "fffff16021790555050610270806101056000396000f3006080604052600436106100415760003" +
                "57c0100000000000000000000000000000000000000000000000000000000900463ffffffff168" +
                "063f2c298be14610046575b600080fd5b34801561005257600080fd5b506100ad6004803603810" +
                "19080803590602001908201803590602001908080601f016020809104026020016040519081016" +
                "04052809392919081815260200183838082843782019150505050505091929192905050506100a" +
                "f565b005b600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1" +
                "673ffffffffffffffffffffffffffffffffffffffff1663f2c298be826040518263ffffffff167" +
                "c01000000000000000000000000000000000000000000000000000000000281526004018080602" +
                "001828103825283818151815260200191508051906020019080838360005b83811015610159578" +
                "08201518184015260208101905061013e565b50505050905090810190601f16801561018657808" +
                "20380516001836020036101000a031916815260200191505b50925050506000604051808303816" +
                "00087803b1580156101a557600080fd5b505af11580156101b9573d6000803e3d6000fd5b50505" +
                "0507f248e1568b00b4eb005baa2f75ec2d472c0c98685c367661e1ffcbdc0f26fdfe1600080905" +
                "4906101000a900473ffffffffffffffffffffffffffffffffffffffff16604051808273fffffff" +
                "fffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1" +
                "6815260200191505060405180910390a1505600a165627a7a723058203a4f281bf5a18502c2caf" +
                "37eac0d1729a184bab65879a54b6481020492b675e10029"

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


    @Test
    fun subscribeContractLogsArrayTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<Boolean>()
        val futureSubscription = FutureTask<List<Log>>()

        framework.subscribeOnContractLogs(
            "1.16.19431",
            "0",
            "0",
            object : UpdateListener<List<Log>> {
                override fun onUpdate(data: List<Log>) {
                    futureSubscription.setComplete(data)
                }
            },
            future.completeCallback()
        )

        thread {
            Thread.sleep(1000)
            callContractEmptyMethod("1.16.19431", "testArrayEvent", framework, EmptyCallback())
        }

        val contractResult = future.get()
        assertNotNull(contractResult)

        val updateResult = futureSubscription.get()
        assertNotNull(updateResult)
    }

    @Test
    fun subscribeContractLogsTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<Boolean>()
        val futureSubscription = FutureTask<List<Log>>()

        framework.subscribeOnContractLogs(
            "1.16.19431",
            "0",
            "0",
            object : UpdateListener<List<Log>> {
                override fun onUpdate(data: List<Log>) {
                    futureSubscription.setComplete(data)
                }
            },
            future.completeCallback()
        )

        thread {
            Thread.sleep(1000)
            callContractEmptyMethod("1.16.19431", "testAddressEvent", framework, EmptyCallback())
        }

        val contractResult = future.get()
        assertNotNull(contractResult)

        val updateResult = futureSubscription.get()
        assertNotNull(updateResult)
    }

    private fun callContractEmptyMethod(
        contractId: String,
        methodName: String,
        framework: EchoFramework,
        callback: Callback<String>
    ) {
        framework.callContract(
            "dima1",
            "P5J8pDyzznMmEdiBCdgB7VKtMBuxw5e4MAJEo3sfUbxcM",
            "1.3.0",
            "1.3.0",
            contractId,
            methodName,
            listOf(),
            callback = callback
        )
    }


    @Test
    fun transferTest() {
        val framework = initFramework()

        val futureTransfer = FutureTask<Boolean>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.sendTransferOperation(
            "dimaty12345",
            "P5JRnzqtPYLxU9ypfndHczCqt178nzomv4DuspTPr1iTf",
            "dariatest2",
            "1", "1.3.0", "1.3.0", "Memasik",
            futureTransfer.completeCallback()
        )

        assertTrue(futureTransfer.get() ?: false)
    }

    @Test
    fun getRequiredFeeTransferOperationTest() {
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
    fun getRequiredTransferOperationFeeFailureTest() {
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
    fun getRequiredContractOperationFeeTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureFee = FutureTask<String>()

        framework.getFeeForContractOperation(
            userNameOrId = "dima1",
            contractId = legalContractId,
            methodName = "incrementCounter",
            methodParams = listOf(),
            assetId = "1.3.0",
            feeAsset = "1.3.0",
            callback = futureFee.completeCallback()
        )

        assertNotNull(futureFee.get())
    }

    @Test(expected = LocalException::class)
    fun getRequiredContractOperationFeeFailureTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureFee = FutureTask<String>()

        framework.getFeeForContractOperation(
            userNameOrId = "dima1",
            contractId = legalContractId,
            methodName = "incrementCounter",
            methodParams = listOf(),
            assetId = "1.3.123456",
            feeAsset = "1.3.123456",
            callback = futureFee.completeCallback()
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

    @Test
    fun createAssetTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureAsset = FutureTask<String>()

        val asset = Asset("").apply {
            symbol = "ASSETM"
            precision = 4
            issuer = Account("1.2.188")
            setBtsOptions(
                BitassetOptions(
                    86400, 7, 86400,
                    100, 2000
                )
            )

            predictionMarket = false
        }

        val options =
            AssetOptions(
                UnsignedLong.valueOf(100000),
                0.toLong(),
                UnsignedLong.ZERO,
                AssetOptions.ALLOW_COMITEE_PROVIDE_FEEDS,
                AssetOptions.ALLOW_COMITEE_PROVIDE_FEEDS,
                Price().apply {
                    this.quote = AssetAmount(UnsignedLong.valueOf(1), Asset("1.3.1"))
                    this.base = AssetAmount(UnsignedLong.valueOf(1), Asset("1.3.0"))
                },
                "description"
            )

        asset.assetOptions = options

        framework.createAsset(
            "dimaty12345", "P5JRnzqtPYLxU9ypfndHczCqt178nzomv4DuspTPr1iTf",
            asset,
            futureAsset.completeCallback()
        )

        assertTrue(futureAsset.get()?.startsWith("1.3.") ?: false)
    }

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

    @Test
    fun createContractTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<String>()

        framework.createContract(
            login,
            password,
            legalAssetId,
            legalAssetId,
            legalContractParamsBytecode,
            callback = object : Callback<String> {
                override fun onSuccess(result: String) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )

        assertTrue(future.get()?.startsWith("1.17.") ?: false)
    }

    @Test
    fun createContractWithParametersTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<String>()

        framework.createContract(
            login,
            password,
            legalAssetId,
            legalAssetId,
            legalContractWithParamsBytecode,
            listOf(InputValue(ContractAddressInputValueType(), "19439")),

            callback = object : Callback<String> {
                override fun onSuccess(result: String) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )

        assertTrue(future.get()?.startsWith("1.17.") ?: false)
    }

    @Test
    fun callContractTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<String>()

        framework.callContract(
            login,
            password,
            legalAssetId,
            legalAssetId,
            legalContractId,
            "incrementCounter",
            listOf(),
            callback = future.completeCallback()
        )

        assertTrue(future.get()?.startsWith("1.17.") ?: false)
    }

    @Test
    fun payableCallContractTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<String>()

        framework.callContract(
            login,
            password,
            legalAssetId,
            legalAssetId,
            legalContractId,
            "incrementCounter",
            listOf(),
            "1000",
            callback = future.completeCallback()
        )

        assertTrue(future.get()?.startsWith("1.17.") ?: false)
    }

    @Test
    fun callContractWithAddressParameterTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<String>()

        val address = "1.2.18"

        framework.callContract(
            ownerId, ownerPassword, "1.3.0", "1.3.0", contractId, "addUserToDoor",
            listOf(
                InputValue(NumberInputValueType("int64"), "3"),
                InputValue(
                    AccountAddressInputValueType(),
                    address
                )
            ),
            callback = future.completeCallback()
        )

        assertTrue(future.get()?.startsWith("1.17.") ?: false)
    }

    @Test
    fun callContractWithStringParameterTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<String>()

        framework.callContract(
            ownerId, ownerPassword, "1.3.0", "1.3.0", contractId, "addDoor",
            listOf(
                InputValue(NumberInputValueType("int64"), "3"),
                InputValue(
                    StringInputValueType(),
                    "door + ${SimpleDateFormat("HH:mm").format(System.currentTimeMillis())}"
                )
            ),
            callback = future.completeCallback()
        )

        assertTrue(future.get()?.startsWith("1.17.") ?: false)
    }

    @Test(expected = LocalException::class)
    fun callContractFailureTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<String>()

        framework.callContract(
            login,
            password,
            legalAssetId,
            legalAssetId,
            illegalContractId,
            "incrementCounter",
            listOf(),
            callback = future.completeCallback()
        )

        assertNull(future.get())
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
            "1.17.34782",
            future.completeCallback()
        )

        val contractResult = future.get()
        assertNotNull(contractResult)
        assertEquals(contractResult!!.execRes.excepted, "None")
    }

    @Test
    fun getContractLogsTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<List<Log>>()

        framework.getContractLogs(
            "1.16.16803",
            "1404100",
            "1404111",
            future.completeCallback()
        )

        val contractResult = future.get()
        assertNotNull(contractResult)
        assert(contractResult!!.isNotEmpty())
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
