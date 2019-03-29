package org.echo.mobile.framework

import com.google.common.primitives.UnsignedLong
import com.google.gson.GsonBuilder
import org.echo.mobile.framework.core.mapper.ObjectMapper
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.AssetOptions
import org.echo.mobile.framework.model.Balance
import org.echo.mobile.framework.model.BitassetOptions
import org.echo.mobile.framework.model.Block
import org.echo.mobile.framework.model.BlockData
import org.echo.mobile.framework.model.DynamicGlobalProperties
import org.echo.mobile.framework.model.FullAccount
import org.echo.mobile.framework.model.GlobalProperties
import org.echo.mobile.framework.model.HistoryResponse
import org.echo.mobile.framework.model.Log
import org.echo.mobile.framework.model.Price
import org.echo.mobile.framework.model.SidechainTransfer
import org.echo.mobile.framework.model.contract.ContractInfo
import org.echo.mobile.framework.model.contract.ContractResult
import org.echo.mobile.framework.model.contract.ContractStruct
import org.echo.mobile.framework.model.contract.input.AccountAddressInputValueType
import org.echo.mobile.framework.model.contract.input.ContractAddressInputValueType
import org.echo.mobile.framework.model.contract.input.ContractInputEncoder
import org.echo.mobile.framework.model.contract.input.InputValue
import org.echo.mobile.framework.model.contract.input.StringInputValueType
import org.echo.mobile.framework.model.contract.toRegular
import org.echo.mobile.framework.model.contract.toX86
import org.echo.mobile.framework.model.network.Echodevnet
import org.echo.mobile.framework.service.UpdateListener
import org.echo.mobile.framework.support.Api
import org.echo.mobile.framework.support.EmptyCallback
import org.echo.mobile.framework.support.Settings
import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.echo.mobile.framework.support.concurrent.future.completeCallback
import org.echo.mobile.framework.support.concurrent.future.wrapResult
import org.echo.mobile.framework.support.dematerialize
import org.echo.mobile.framework.support.fold
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Test cases for [EchoFramework]
 *
 * @author Dmitriy Bushuev
 */
class EchoFrameworkTest {
    private fun initFramework(ratio: BigDecimal = BigDecimal.ONE): EchoFramework {
        return EchoFramework.create(
            Settings.Configurator()
                .setUrl("wss://devnet.echo-dev.io/ws")
                .setNetworkType(Echodevnet())
                .setReturnOnMainThread(false)
                .setApis(
                    Api.DATABASE,
                    Api.NETWORK_BROADCAST,
                    Api.ACCOUNT_HISTORY,
                    Api.REGISTRATION
                )
                .setFeeRatio(ratio)
                .configure()
        )
    }

    private val legalContractId = "1.16.106"
    private val legalTokenId = "1.16.103"
    private val accountId = "1.2.14"
    private val login = "dima"
    private val password = "dima"
    private val wif = "5J3UbadSyzzcQQ7HEfTr2brhJJpHhx3NsMzrvgzfysBesutNRCm"
    private val secondAccountId = "1.2.15"
    private val secondLogin = "daria"
    private val secondPassword = "daria"
    private val legalAssetId = "1.3.0"

    private val legalContractParamsBytecode =
        "60806040526000805534801561001457600080fd5b506104e180610024600" +
                "0396000f3fe608060405260043610610088576000357c01000000000000000000000000000000000000" +
                "00000000000000000000900480631361c3941461008d5780635b34b966146100a45780637a3a86b2146" +
                "100bb578063a87d942c1461014c578063b835666314610177578063c820d1b21461018e578063e13a77" +
                "16146102cf578063f5c5ad8314610319575b600080fd5b34801561009957600080fd5b506100a261033" +
                "0565b005b3480156100b057600080fd5b506100b961039a565b005b3480156100c757600080fd5b5061" +
                "010a600480360360208110156100de57600080fd5b81019080803573fffffffffffffffffffffffffff" +
                "fffffffffffff1690602001909291905050506103ac565b604051808273ffffffffffffffffffffffff" +
                "ffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019150506040" +
                "5180910390f35b34801561015857600080fd5b506101616103b6565b604051808281526020019150506" +
                "0405180910390f35b34801561018357600080fd5b5061018c6103bf565b005b34801561019a57600080" +
                "fd5b50610254600480360360208110156101b157600080fd5b81019080803590602001906401000000" +
                "008111156101ce57600080fd5b8201836020820111156101e057600080fd5b803590602001918460018" +
                "3028401116401000000008311171561020257600080fd5b91908080601f016020809104026020016040" +
                "519081016040528093929190818152602001838380828437600081840152601f19601f8201169050808" +
                "30192505050505050509192919290505050610491565b60405180806020018281038252838181518152" +
                "60200191508051906020019080838360005b838110156102945780820151818401526020810190506102" +
                "79565b50505050905090810190601f1680156102c15780820380516001836020036101000a0319168152" +
                "60200191505b509250505060405180910390f35b6102d761049b565b604051808273fffffffffffffff" +
                "fffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001915" +
                "05060405180910390f35b34801561032557600080fd5b5061032e6104a3565b005b7f65e06b3884a88e1" +
                "3243953e5160aec7a836e4cc7abc0762f84c66d6b918efe7f60405180806020018281038252600981526" +
                "02001807f6c6f67206576656e7400000000000000000000000000000000000000000000008152506020" +
                "0191505060405180910390a1565b60016000808282540192505081905550565b6000819050919050565" +
                "b60008054905090565b7f65e06b3884a88e13243953e5160aec7a836e4cc7abc0762f84c66d6b918efe" +
                "7f60405180806020018281038252600f8152602001807f6c6f67206576656e742066697273740000000" +
                "00000000000000000000000000081525060200191505060405180910390a17f65e06b3884a88e1324395" +
                "3e5160aec7a836e4cc7abc0762f84c66d6b918efe7f60405180806020018281038252601081526020018" +
                "07f6c6f67206576656e74207365636f6e640000000000000000000000000000000081525060200191505" +
                "060405180910390a1565b6060819050919050565b600033905090565b600160008082825403925050819" +
                "0555056fea165627a7a7230582093e5f08daa13bf9bc7c17df95e119be299398beca796aa23631e9174" +
                "4285958f0029"

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

    private val legalTokenBytecode = "60806040526005600260006101000a81548160ff021916908360ff1602" +
            "1790555034801561002c57600080fd5b50604051610c72380380610c728339810180604052606081101" +
            "561004f57600080fd5b8101908080519060200190929190805164010000000081111561007157600080" +
            "fd5b8281019050602081018481111561008757600080fd5b81518560018202830111640100000000821" +
            "117156100a457600080fd5b505092919060200180516401000000008111156100c057600080fd5b8281" +
            "01905060208101848111156100d657600080fd5b8151856001820283011164010000000082111715610" +
            "0f357600080fd5b5050929190505050600260009054906101000a900460ff1660ff16600a0a83026003" +
            "81905550600354600460003373ffffffffffffffffffffffffffffffffffffffff1673fffffffffffff" +
            "fffffffffffffffffffffffffff16815260200190815260200160002081905550816000908051906020" +
            "0190610175929190610195565b50806001908051906020019061018c929190610195565b50505050610" +
            "23a565b828054600181600116156101000203166002900490600052602060002090601f016020900481" +
            "019282601f106101d657805160ff1916838001178555610204565b82800160010185558215610204579" +
            "182015b828111156102035782518255916020019190600101906101e8565b5b50905061021191906102" +
            "15565b5090565b61023791905b8082111561023357600081600090555060010161021b565b5090565b9" +
            "0565b610a29806102496000396000f3fe608060405260043610610088576000357c0100000000000000" +
            "0000000000000000000000000000000000000000009004806306fdde031461008d57806318160ddd146" +
            "1011d57806323b872dd14610148578063313ce567146101db57806370a082311461020c57806395d89b" +
            "4114610271578063a9059cbb14610301578063dd62ed3e14610374575b600080fd5b348015610099576" +
            "00080fd5b506100a26103f9565b60405180806020018281038252838181518152602001915080519060" +
            "20019080838360005b838110156100e25780820151818401526020810190506100c7565b50505050905" +
            "090810190601f16801561010f5780820380516001836020036101000a031916815260200191505b5092" +
            "50505060405180910390f35b34801561012957600080fd5b50610132610497565b60405180828152602" +
            "00191505060405180910390f35b34801561015457600080fd5b506101c1600480360360608110156101" +
            "6b57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff1690602001909291" +
            "90803573ffffffffffffffffffffffffffffffffffffffff16906020019092919080359060200190929" +
            "19050505061049d565b604051808215151515815260200191505060405180910390f35b3480156101e" +
            "757600080fd5b506101f06105ca565b604051808260ff1660ff16815260200191505060405180910390" +
            "f35b34801561021857600080fd5b5061025b6004803603602081101561022f57600080fd5b810190808" +
            "03573ffffffffffffffffffffffffffffffffffffffff1690602001909291905050506105dd565b6040" +
            "518082815260200191505060405180910390f35b34801561027d57600080fd5b506102866105f5565b6" +
            "040518080602001828103825283818151815260200191508051906020019080838360005b8381101561" +
            "02c65780820151818401526020810190506102ab565b50505050905090810190601f1680156102f35780" +
            "820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801" +
            "561030d57600080fd5b5061035a6004803603604081101561032457600080fd5b81019080803573fffff" +
            "fffffffffffffffffffffffffffffffffff169060200190929190803590602001909291905050506106" +
            "93565b604051808215151515815260200191505060405180910390f35b34801561038057600080fd5b50" +
            "6103e36004803603604081101561039757600080fd5b81019080803573ffffffffffffffffffffffffff" +
            "ffffffffffffff169060200190929190803573ffffffffffffffffffffffffffffffffffffffff1690602" +
            "001909291905050506106aa565b6040518082815260200191505060405180910390f35b6000805460018" +
            "1600116156101000203166002900480601f016020809104026020016040519081016040528092919081" +
            "81526020018280546001816001161561010002031660029004801561048f5780601f106104645761010" +
            "080835404028352916020019161048f565b820191906000526020600020905b81548152906001019060" +
            "200180831161047257829003601f168201915b505050505081565b60035481565b6000600560008573f" +
            "fffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff" +
            "16815260200190815260200160002060003373ffffffffffffffffffffffffffffffffffffffff1673fff" +
            "fffffffffffffffffffffffffffffffffffff16815260200190815260200160002054821115151561052" +
            "a57600080fd5b81600560008673ffffffffffffffffffffffffffffffffffffffff1673fffffffffffff" +
            "fffffffffffffffffffffffffff16815260200190815260200160002060003373fffffffffffffffffff" +
            "fffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152" +
            "602001600020600082825403925050819055506105bf8484846106cf565b600190509392505050565b60" +
            "0260009054906101000a900460ff1681565b60046020528060005260406000206000915090505481565" +
            "b60018054600181600116156101000203166002900480601f0160208091040260200160405190810160" +
            "4052809291908181526020018280546001816001161561010002031660029004801561068b5780601f1" +
            "06106605761010080835404028352916020019161068b565b820191906000526020600020905b8154815" +
            "2906001019060200180831161066e57829003601f168201915b505050505081565b60006106a03384846" +
            "106cf565b6001905092915050565b6005602052816000526040600020602052806000526040600020600" +
            "091509150505481565b600073ffffffffffffffffffffffffffffffffffffffff168273fffffffffffff" +
            "fffffffffffffffffffffffffff161415151561070b57600080fd5b80600460008573ffffffffffffff" +
            "ffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190" +
            "8152602001600020541015151561075957600080fd5b600460008373ffffffffffffffffffffffffffff" +
            "ffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000" +
            "205481600460008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffff" +
            "ffffffffffffffffff1681526020019081526020016000205401101515156107e857600080fd5b600060" +
            "0460008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffff" +
            "ffffffffff16815260200190815260200160002054600460008673fffffffffffffffffffffffffffff" +
            "fffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002" +
            "05401905081600460008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffff" +
            "ffffffffffffffffffffffff16815260200190815260200160002060008282540392505081905550816" +
            "00460008573ffffffffffffffffffffffffffffffffffffffff1673fffffffffffffffffffffffffffff" +
            "fffffffffff168152602001908152602001600020600082825401925050819055508273fffffffffffff" +
            "fffffffffffffffffffffffffff168473ffffffffffffffffffffffffffffffffffffffff167fddf252a" +
            "d1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef84604051808281526020019150" +
            "5060405180910390a380600460008573ffffffffffffffffffffffffffffffffffffffff1673ffffffff" +
            "ffffffffffffffffffffffffffffffff16815260200190815260200160002054600460008773ffffffff" +
            "ffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526" +
            "0200190815260200160002054011415156109f757fe5b5050505056fea165627a7a723058207d7021bb" +
            "4a32fc7065227e255004298654e202377452c0c4e83d3250f11a38ca002900000000000000000000000" +
            "00000000000000000000000000000000005f5e100000000000000000000000000000000000000000000" +
            "000000000000000000006000000000000000000000000000000000000000000000000000000000000000" +
            "a0000000000000000000000000000000000000000000000000000000000000000b53696d706c65546f6" +
            "b656e000000000000000000000000000000000000000000000000000000000000000000000000000000" +
            "000000000000000000000000000353544e0000000000000000000000000000000000000000000000000" +
            "000000000"

    private val illegalContractId = "1.16.-1"
    private val illegalHistoryItemId = "1.17.-1"

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
            accountId,
            "1.11.1",
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
            login,
            password,
            futureLogin.completeCallback()
        )

        val account = futureLogin.get()
        assertTrue(account != null)

        val futureLoginFailure = FutureTask<FullAccount>()

        framework.isOwnedBy(
            login,
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

        framework.getAccount(login, futureAccount.completeCallback())

        val account = futureAccount.get()
        assertTrue(account != null)
    }

    @Test
    fun getAccountByWifTest() {
        val framework = initFramework()

        val futureAccounts = FutureTask<List<FullAccount>>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.getAccountsByWif(
            "5J3UbadSyzzcQQ7HEfTr2brhJJpHhx3NsMzrvgzfysBesutNRCm",
            futureAccounts.completeCallback()
        )

        val accounts = futureAccounts.get()
        Assert.assertTrue(accounts?.isNotEmpty() == true)
    }

    @Test
    fun checkAccountReservedTest() {
        val framework = initFramework()

        val futureCheckReserved = FutureTask<Boolean>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.checkAccountReserved(login, futureCheckReserved.completeCallback())

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

        framework.getBalance(login, legalAssetId, futureBalanceExistent.completeCallback())

        assertTrue(futureBalanceExistent.get() != null)
    }

    @Test(expected = LocalException::class)
    fun getNonexistentAssetBalanceTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureBalanceNonexistent = FutureTask<Balance>()

        framework.getBalance(
            login,
            "ergergger",
            futureBalanceNonexistent.completeCallback()
        )

        assertNotNull(futureBalanceNonexistent.get())
    }

    @Test
    fun accountHistoryByIdTest() = getAccountHistory(accountId)

    @Test
    fun accountHistoryByNameTest() = getAccountHistory("dima")

    private fun getAccountHistory(nameOrId: String) {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureAccountHistory = FutureTask<HistoryResponse>()

        framework.getAccountHistory(
            nameOrId,
            "1.11.1",
            "1.11.0",
            100,
            futureAccountHistory.completeCallback()
        )

        val history = futureAccountHistory.get()

        assertNotNull(history)
        assertTrue(history?.transactions?.isNotEmpty() ?: false)
        assertNotNull(history?.transactions?.get(0)?.blockNum)
    }

    @Test
    fun changePasswordTest() {
        val framework = initFramework()

        val futureChangePassword = FutureTask<Boolean>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.changePassword(
            login, password, password,
            object : Callback<Any> {
                override fun onSuccess(result: Any) {
                    futureChangePassword.setComplete(true)
                }

                override fun onError(error: LocalException) {
                    futureChangePassword.setComplete(false)
                }

            })

        assertTrue(futureChangePassword.get() ?: false)
    }

//    @Test
//    fun registrationTest() {
//        val framework = initFramework()
//
//        val futureChangePassword = FutureTask<Boolean>()
//
//        if (connect(framework) == false) Assert.fail("Connection error")
//
//        framework.register(
//            "daria", "daria",
//            object : Callback<Boolean> {
//                override fun onSuccess(result: Boolean) {
//                    futureChangePassword.setComplete(true)
//                }
//
//                override fun onError(error: LocalException) {
//                    futureChangePassword.setComplete(false)
//                }
//
//            })
//
//        assertTrue(futureChangePassword.get() ?: false)
//    }

    @Test
    fun subscriptionByIdTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureSubscriptionById = FutureTask<FullAccount>()
        val futureSubscriptionResult = FutureTask<Boolean>()

        framework.subscribeOnAccount(accountId, object : AccountListener {

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

        framework.subscribeOnAccount(login, object : AccountListener {
            override fun onChange(updatedAccount: FullAccount) {
                futureSubscriptionByName.setComplete(updatedAccount)
            }

        }, futureSubscriptionResult.completeCallback())

        thread {
            Thread.sleep(3000)
            sendAmount(framework, EmptyCallback())
        }

        assertNotNull(futureSubscriptionByName.get())
        assertTrue(futureSubscriptionResult.get() ?: false)
    }

    private fun sendAmount(framework: EchoFramework, callback: Callback<Boolean>) {
        framework.sendTransferOperation(
            login,
            password,
            toNameOrId = secondLogin,
            amount = "1",
            asset = legalAssetId,
            feeAsset = legalAssetId,
            message = null,
            callback = callback
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

        framework.subscribeOnAccount(login, object : AccountListener {
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
            legalContractId,
            listener = object : UpdateListener<List<Log>> {
                override fun onUpdate(data: List<Log>) {
                    futureSubscription.setComplete(data)
                }
            },
            callback = future.completeCallback()
        )

        thread {
            Thread.sleep(1000)
            callContractWithEmptyParams(
                legalContractId,
                "logTestArray",
                framework,
                EmptyCallback()
            )
        }

        val contractResult = future.get()
        assertNotNull(contractResult)

        val updateResult = futureSubscription.get()
        assertNotNull(updateResult)
        assert(updateResult!!.isNotEmpty())
    }

    @Test
    fun subscribeContractLogsTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<Boolean>()
        val futureSubscription = FutureTask<List<Log>>()

        framework.subscribeOnContractLogs(
            legalContractId,
            listener = object : UpdateListener<List<Log>> {
                override fun onUpdate(data: List<Log>) {
                    futureSubscription.setComplete(data)
                }
            },
            callback = future.completeCallback()
        )

        thread {
            Thread.sleep(1000)
            callContractWithEmptyParams(
                legalContractId,
                "logTest",
                framework,
                EmptyCallback()
            )
        }

        val contractResult = future.get()
        assertNotNull(contractResult)

        val updateResult = futureSubscription.get(30, TimeUnit.SECONDS)
        assertNotNull(updateResult)
        assert(updateResult!!.isNotEmpty())
    }

    private fun callContractWithEmptyParams(
        contractId: String,
        methodName: String,
        framework: EchoFramework,
        sentCallback: Callback<Boolean>
    ) {
        framework.callContract(
            userNameOrId = login,
            password = password,
            assetId = legalAssetId,
            feeAsset = legalAssetId,
            contractId = legalContractId,
            methodName = methodName,
            methodParams = listOf(),
            broadcastCallback = sentCallback
        )
    }

    @Test
    fun transferTest() {
        val framework = initFramework()

        val futureTransfer = FutureTask<Boolean>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.sendTransferOperation(
            login,
            password,
            toNameOrId = secondLogin,
            amount = "1",
            asset = legalAssetId,
            feeAsset = legalAssetId,
            message = "Memasik",
            callback = futureTransfer.completeCallback()
        )

        assertTrue(futureTransfer.get() ?: false)
    }

    @Test
    fun transferWithWifTest() {
        val framework = initFramework()

        val futureTransfer = FutureTask<Boolean>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.sendTransferOperationWithWif(
            login,
            wif,
            toNameOrId = secondLogin,
            amount = "1",
            asset = legalAssetId,
            feeAsset = legalAssetId,
            message = "Memasik",
            callback = futureTransfer.completeCallback()
        )

        assertTrue(futureTransfer.get() ?: false)
    }

    @Test
    fun getRequiredFeeTransferOperationTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureFee = FutureTask<String>()

        framework.getFeeForTransferOperation(
            login,
            password,
            secondLogin,
            amount = "10000",
            asset = legalAssetId,
            feeAsset = legalAssetId,
            message = "Memasiki",
            callback = futureFee.completeCallback()
        )

        assertNotNull(futureFee.get())
    }

    @Test
    fun getRequiredFeeTransferOperationWithWifTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureFee = FutureTask<String>()

        framework.getFeeForTransferOperationWithWif(
            login,
            wif,
            secondLogin,
            amount = "10000",
            asset = legalAssetId,
            feeAsset = legalAssetId,
            message = "Memasiki",
            callback = futureFee.completeCallback()
        )

        assertNotNull(futureFee.get())
    }

    @Test(expected = LocalException::class)
    fun getRequiredTransferOperationFeeFailureTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureFee = FutureTask<String>()

        framework.getFeeForTransferOperation(
            login,
            password,
            secondLogin,
            "10000",
            "1.3.1234",
            "1.3.1234",
            "Ololoshka",
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
            userNameOrId = accountId,
            contractId = legalContractId,
            amount = "0",
            methodName = "testReturn",
            methodParams = listOf(),
            assetId = legalAssetId,
            feeAsset = legalAssetId,
            callback = futureFee.completeCallback()
        )

        assertNotNull(futureFee.get())
    }

    @Test
    fun getRequiredContractOperationFeeWithCodeTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureFee = FutureTask<String>()

        framework.getFeeForContractOperation(
            userNameOrId = accountId,
            contractId = legalContractId,
            amount = "0",
            code = "e13a7716",
            assetId = legalAssetId,
            feeAsset = legalAssetId,
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
            userNameOrId = login,
            contractId = legalContractId,
            amount = "0",
            methodName = "testReturn",
            methodParams = listOf(),
            assetId = legalAssetId,
            feeAsset = "1.3.123123123",
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
            lowerBound = "ECHO", limit = 10,
            callback = futureAssets.completeCallback()
        )

        assertNotNull(futureAssets.get()?.isNotEmpty() ?: false)
    }

    @Test
    fun getAssetsTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureAssets = FutureTask<List<Asset>>()

        framework.getAssets(
            listOf("1.3.0", "1.3.150"),
            futureAssets.completeCallback()
        )

        val assets = futureAssets.get()
        assertNotNull(assets)
        assertEquals(2, assets?.size ?: 0)
        assertEquals(assets?.firstOrNull()?.symbol, "ECHO")
    }

    @Test
    fun lookupAssetsSymbols() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureAssets = FutureTask<List<Asset>>()

        framework.lookupAssetsSymbols(
            listOf("ECHO", "1.3.0"),
            futureAssets.completeCallback()
        )

        val assets = futureAssets.get()
        assertNotNull(assets)
        assertEquals(assets?.size, 2)
        assertEquals(assets?.firstOrNull()?.getObjectId(), "1.3.0")
        assertEquals(assets?.get(1)?.symbol, "ECHO")
    }

//    @Test
//    fun createAssetTest() {
//        val framework = initFramework()
//
//        if (connect(framework) == false) Assert.fail("Connection error")
//
//        val futureAsset = FutureTask<String>()
//        val broadcastFuture = FutureTask<Boolean>()
//
//        val asset = configureAsset()
//
//        framework.createAsset(
//            login,
//            password,
//            asset,
//            broadcastFuture.completeCallback(),
//            futureAsset.completeCallback()
//        )
//
//        assertTrue(broadcastFuture.get() ?: false)
//        assertTrue(futureAsset.get()?.startsWith("1.3.") ?: false)
//    }
//
//
//    @Test
//    fun createAssetWithWifTest() {
//        val framework = initFramework()
//
//        if (connect(framework) == false) Assert.fail("Connection error")
//
//        val futureAsset = FutureTask<String>()
//        val broadcastFuture = FutureTask<Boolean>()
//
//        val asset = configureAsset()
//
//        framework.createAssetWithWif(
//            login, wif,
//            asset,
//            broadcastFuture.completeCallback(),
//            futureAsset.completeCallback()
//        )
//
//        assertTrue(broadcastFuture.get() ?: false)
//        assertTrue(futureAsset.get()?.startsWith("1.3.") ?: false)
//    }

    @Test
    fun issueAssetTest() {
        val framework = initFramework()

        val futureIssue = FutureTask<Boolean>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.issueAsset(
            login,
            password,
            asset = "1.3.6",
            amount = "1",
            destinationIdOrName = login,
            message = "Do it",
            callback = futureIssue.completeCallback()
        )

        assertTrue(futureIssue.get() ?: false)
    }

    @Test
    fun issueAssetWithWifTest() {
        val framework = initFramework()

        val futureIssue = FutureTask<Boolean>()

        if (connect(framework) == false) Assert.fail("Connection error")

        framework.issueAssetWithWif(
            login,
            wif,
            asset = "1.3.6",
            amount = "1",
            destinationIdOrName = login,
            message = "Do it",
            callback = futureIssue.completeCallback()
        )

        assertTrue(futureIssue.get() ?: false)
    }

    @Test
    fun createContractTest() {
        val framework = initFramework(BigDecimal.valueOf(100))

        if (connect(framework) == false) Assert.fail("Connection error")

        val broadcastFuture = FutureTask<Boolean>()
        val future = FutureTask<String>()

        framework.createContract(
            login,
            password,
            assetId = legalAssetId,
            feeAsset = legalAssetId,
            byteCode = legalTokenBytecode,
            broadcastCallback = broadcastFuture.completeCallback(),
            resultCallback = future.completeCallback()
        )

        assertTrue(broadcastFuture.get() ?: false)
        assertTrue(future.get()?.startsWith("1.17.") ?: false)
    }

    @Test
    fun createContractWithWifTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val broadcastFuture = FutureTask<Boolean>()
        val future = FutureTask<String>()

        framework.createContractWithWif(
            login,
            wif,
            assetId = legalAssetId,
            feeAsset = legalAssetId,
            byteCode = legalTokenBytecode,
            broadcastCallback = broadcastFuture.completeCallback(),
            resultCallback = future.completeCallback()
        )

        assertTrue(broadcastFuture.get() ?: false)
        assertTrue(future.get()?.startsWith("1.17.") ?: false)
    }

    @Test
    fun createContractWithParametersTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val broadcastFuture = FutureTask<Boolean>()
        val future = FutureTask<String>()

        framework.createContract(
            login,
            password,
            assetId = legalAssetId,
            feeAsset = legalAssetId,
            byteCode = legalContractWithParamsBytecode,
            params = listOf(InputValue(ContractAddressInputValueType(), "2587")),
            broadcastCallback = broadcastFuture.completeCallback(),
            resultCallback = future.completeCallback()
        )

        assertTrue(broadcastFuture.get() ?: false)
        assertTrue(future.get()?.startsWith("1.17.") ?: false)
    }

    @Test
    fun callContractTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val broadcastFuture = FutureTask<Boolean>()
        val future = FutureTask<String>()

        framework.callContract(
            login,
            password,
            assetId = legalAssetId,
            feeAsset = legalAssetId,
            contractId = legalContractId,
            methodName = "logTest",
            methodParams = listOf(),
            broadcastCallback = broadcastFuture.completeCallback(),
            resultCallback = future.completeCallback()
        )

        assertTrue(broadcastFuture.get() ?: false)
        assertTrue(future.get()?.startsWith("1.17.") ?: false)
    }

    @Test
    fun callContractWithRatioTest() {
        val framework = initFramework(BigDecimal.valueOf(100))

        if (connect(framework) == false) Assert.fail("Connection error")

        val broadcastFuture = FutureTask<Boolean>()
        val future = FutureTask<String>()

        framework.callContract(
            login,
            password,
            assetId = legalAssetId,
            feeAsset = legalAssetId,
            contractId = legalContractId,
            methodName = "logTest",
            methodParams = listOf(),
            broadcastCallback = broadcastFuture.completeCallback(),
            resultCallback = future.completeCallback()
        )

        assertTrue(broadcastFuture.get() ?: false)
        assertTrue(future.get()?.startsWith("1.17.") ?: false)
    }

    @Test
    fun callContractUsingCodeTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val broadcastFuture = FutureTask<Boolean>()
        val future = FutureTask<String>()

        val contractCode = ContractInputEncoder().encode("logTest", listOf())

        framework.callContract(
            login,
            password,
            assetId = legalAssetId,
            feeAsset = legalAssetId,
            contractId = legalContractId,
            code = contractCode,
            broadcastCallback = broadcastFuture.completeCallback(),
            resultCallback = future.completeCallback()
        )

        assertTrue(broadcastFuture.get() ?: false)
        assertTrue(future.get()?.startsWith("1.17.") ?: false)
    }

    @Test
    fun callContractWithWifTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val broadcastFuture = FutureTask<Boolean>()
        val future = FutureTask<String>()

        framework.callContractWithWif(
            login,
            wif,
            assetId = legalAssetId,
            feeAsset = legalAssetId,
            contractId = legalContractId,
            methodName = "logTest",
            methodParams = listOf(),
            broadcastCallback = broadcastFuture.completeCallback(),
            resultCallback = future.completeCallback()
        )

        assertTrue(broadcastFuture.get() ?: false)
        assertTrue(future.get()?.startsWith("1.17.") ?: false)
    }

    @Test
    fun callContractWithWifUsingCodeTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val broadcastFuture = FutureTask<Boolean>()
        val future = FutureTask<String>()

        val contractCode = ContractInputEncoder().encode("logTest", listOf())

        framework.callContractWithWif(
            login,
            wif,
            assetId = legalAssetId,
            feeAsset = legalAssetId,
            contractId = legalContractId,
            code = contractCode,
            broadcastCallback = broadcastFuture.completeCallback(),
            resultCallback = future.completeCallback()
        )

        assertTrue(broadcastFuture.get() ?: false)
        assertTrue(future.get()?.startsWith("1.17.") ?: false)
    }

    @Test
    fun callContractWithAccountSubscriptionTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val futureSubscriptionByName = FutureTask<FullAccount>()
        val futureSubscriptionResult = FutureTask<Boolean>()

        framework.subscribeOnAccount(login, object : AccountListener {
            override fun onChange(updatedAccount: FullAccount) {
                futureSubscriptionByName.setComplete(updatedAccount)
            }

        }, futureSubscriptionResult.completeCallback())

        val broadcastFuture = FutureTask<Boolean>()
        val future = FutureTask<String>()

        framework.callContract(
            login,
            password,
            assetId = legalAssetId,
            feeAsset = legalAssetId,
            contractId = legalContractId,
            methodName = "testReturn",
            methodParams = listOf(),
            broadcastCallback = broadcastFuture.completeCallback(),
            resultCallback = future.completeCallback()
        )

        assertTrue(broadcastFuture.get() ?: false)
        assertTrue(future.get(15, TimeUnit.SECONDS)?.startsWith("1.17.") ?: false)
    }

    @Test
    fun payableCallContractTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val broadcastFuture = FutureTask<Boolean>()
        val future = FutureTask<String>()

        framework.callContract(
            login,
            password,
            legalAssetId,
            legalAssetId,
            legalContractId,
            "testReturn",
            listOf(),
            "1000",
            broadcastCallback = broadcastFuture.completeCallback(),
            resultCallback = future.completeCallback()
        )

        assertTrue(broadcastFuture.get() ?: false)
        assertTrue(future.get()?.startsWith("1.17.") ?: false)
    }

    @Test
    fun callContractWithAddressParameterTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val broadcastFuture = FutureTask<Boolean>()
        val future = FutureTask<String>()

        val address = accountId

        framework.callContract(
            login, password,
            assetId = legalAssetId,
            feeAsset = legalAssetId,
            contractId = legalContractId,
            methodName = "testAddressParameter",
            methodParams = listOf(InputValue(AccountAddressInputValueType(), address)),
            broadcastCallback = broadcastFuture.completeCallback(),
            resultCallback = future.completeCallback()
        )

        assertTrue(broadcastFuture.get() ?: false)
        assertTrue(future.get()?.startsWith("1.17.") ?: false)
    }

    @Test
    fun callContractWithStringParameterTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val broadcastFuture = FutureTask<Boolean>()
        val future = FutureTask<String>()

        framework.callContract(
            login, password,
            assetId = legalAssetId,
            feeAsset = legalAssetId,
            contractId = legalContractId,
            methodName = "testStringParameter",
            methodParams = listOf(
                InputValue(StringInputValueType(), "door123")
            ),
            broadcastCallback = broadcastFuture.completeCallback(),
            resultCallback = future.completeCallback()
        )

        assertTrue(broadcastFuture.get() ?: false)
        assertTrue(future.get()?.startsWith("1.17.") ?: false)
    }

    @Test(expected = LocalException::class)
    fun callContractFailureTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val broadcastFuture = FutureTask<Boolean>()
        val future = FutureTask<String>()

        framework.callContract(
            login,
            password,
            assetId = legalAssetId,
            feeAsset = legalAssetId,
            contractId = illegalContractId,
            methodName = "incrementCounter",
            methodParams = listOf(),
            broadcastCallback = broadcastFuture.completeCallback(),
            resultCallback = future.completeCallback()
        )

        assertFalse(broadcastFuture.get() ?: false)
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
            contractId = legalTokenId,
            methodName = "balanceOf",
            methodParams = listOf(
                InputValue(
                    AccountAddressInputValueType(),
                    accountId
                )
            ),
            callback = future.completeCallback()
        )

        assertNotNull(future.get())
        assert(future.get()!!.isNotEmpty())
    }

    @Test
    fun queryContractWithCodeTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<String>()

        framework.queryContract(
            login,
            legalAssetId,
            contractId = legalTokenId,
            code = "70a08231000000000000000000000000000000000000000000000000000000000000000e",
            callback = future.completeCallback()
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
            assetId = legalAssetId,
            contractId = illegalContractId,
            methodName = "testReturn",
            methodParams = listOf(),
            callback = future.completeCallback()
        )

        assertNotNull(future.get())
        assert(future.get()!!.isEmpty())
    }

    @Test
    fun getRegularContractResultTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<ContractResult>()

        framework.getContractResult(
            historyId = "1.17.41",
            callback = future.completeCallback()
        )

        val contractResult = future.get()
        assertNotNull(contractResult)

        assertNotNull(contractResult?.toRegular())
    }

    @Test
    fun getContractResultx86Test() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<ContractResult>()

        framework.getContractResult(
            historyId = "1.17.0",
            callback = future.completeCallback()
        )

        val contractResult = future.get()
        assertNotNull(contractResult)

        contractResult?.toX86()
    }

    @Test
    fun getContractLogsTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<List<Log>>()

        framework.getContractLogs(
            contractId = legalContractId,
            fromBlock = "0",
            toBlock = "9999999",
            callback = future.completeCallback()
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

        future.get()
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
    fun getRegularContractTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<ContractStruct>()

        framework.getContract(
            legalContractId,
            future.completeCallback()
        )

        val contractResult = future.get()
        assertNotNull(contractResult)
    }

    @Test
    fun getx86ContractTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<ContractStruct>()

        framework.getContract(
            "1.16.1",
            future.completeCallback()
        )

        val contractResult = future.get()
        assertNotNull(contractResult)
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

    @Test
    fun getGlobalPropertiesTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<GlobalProperties>()

        framework.getGlobalProperties(future.completeCallback())

        val properties = future.get()
        assertNotNull(properties)
    }

    @Test
    fun getSidechainTransfersTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val future = FutureTask<List<SidechainTransfer>>()

        framework.getSidechainTransfers(
            "0x46Ba2677a1c982B329A81f60Cf90fBA2E8CA9fA8",
            future.completeCallback()
        )

        val transfers = future.get()
        assertNotNull(transfers)
    }

    @Test
    fun getObjectTest() {
        val framework = initFramework()

        if (connect(framework) == false) Assert.fail("Connection error")

        val transferId = "1.19.2"

        val sidechainTransferResult = framework.databaseApiService.getObjects(
            listOf(transferId),
            object : ObjectMapper<SidechainTransfer> {

                override fun map(data: String): SidechainTransfer? {
                    val gson = GsonBuilder().create()

                    return gson.fromJson(data, SidechainTransfer::class.java)
                }

            })

        assertNotNull(sidechainTransferResult)
        assertNotNull(sidechainTransferResult.dematerialize())
        assertEquals(transferId, sidechainTransferResult.dematerialize()[0].getObjectId())
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

    private fun configureAsset(): Asset {
        val asset = Asset("").apply {
            symbol = "ANDRASSET"
            precision = 4
            issuer = Account(accountId)
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
                UnsignedLong.valueOf(100000000000),
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

        return asset
    }

}
