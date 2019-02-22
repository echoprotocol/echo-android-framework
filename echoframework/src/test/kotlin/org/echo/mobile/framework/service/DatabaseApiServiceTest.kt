package org.echo.mobile.framework.service

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.crypto.internal.CryptoCoreComponentImpl
import org.echo.mobile.framework.core.crypto.internal.eddsa.key.IrohaKeyPairCryptoAdapter
import org.echo.mobile.framework.core.socket.SocketCoreComponent
import org.echo.mobile.framework.core.socket.SocketMessengerListener
import org.echo.mobile.framework.core.socket.SocketState
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.Block
import org.echo.mobile.framework.model.DynamicGlobalProperties
import org.echo.mobile.framework.model.FullAccount
import org.echo.mobile.framework.model.contract.ContractInfo
import org.echo.mobile.framework.model.contract.ContractResult
import org.echo.mobile.framework.model.contract.ContractStruct
import org.echo.mobile.framework.model.contract.ContractType
import org.echo.mobile.framework.model.contract.toRegular
import org.echo.mobile.framework.model.contract.toX86
import org.echo.mobile.framework.model.network.Echodevnet
import org.echo.mobile.framework.model.socketoperations.FullAccountsSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetAssetsSocketOperation
import org.echo.mobile.framework.model.socketoperations.SocketOperation
import org.echo.mobile.framework.service.internal.DatabaseApiServiceImpl
import org.echo.mobile.framework.support.error
import org.echo.mobile.framework.support.value
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.util.Date

/**
 * Test cases for [DatabaseApiServiceImpl]
 *
 * @author Dmitriy Bushuev
 */
class DatabaseApiServiceTest {

    private lateinit var fullAccountsResponse: Map<String, FullAccount>
    private lateinit var dynamicGlobalPropertiesResponse: DynamicGlobalProperties
    private lateinit var blockResponse: Block
    private val cryptoCorComponnet by lazy {
        CryptoCoreComponentImpl(
            Echodevnet(),
            IrohaKeyPairCryptoAdapter()
        )
    }

    @Before
    fun setUp() {
        fullAccountsResponse = mapOf("1.2.18" to FullAccount(), "1.2.19" to FullAccount())
        dynamicGlobalPropertiesResponse = DynamicGlobalProperties(
            id = "2.1.0",
            headBlockId = "000033affe5ec1c922b4666f3e754233b31af00e",
            date = Date(),
            currentWitness = "1.6.6",
            nextMaintenanceDate = Date(),
            recentSlotsFilled = "340282366920938463463374607431768211455",
            lastBudgetTime = "budgetTime"
        )
        blockResponse = Block(
            "000000ea71c0589dc4f1db03fa38d38675b538f3",
            "2018-08-02T14:42:30",
            "1.6.11",
            "405847ef0a456fdf7d8ae1aa6458a7edb6491695",
            "0000000000000000000000000000000000000000000000000000000" +
                    "000000000000000000000000000000000000000000000000000000000000000000000000000",
            listOf()
        )
    }

    @Test
    fun getFullAccountsTest() {
        val socketCoreComponent = DatabaseApiServiceSocketCoreComponentMock()

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.getFullAccounts(
            listOf("1.2.18", "1.2.19"),
            false,
            object : Callback<Map<String, FullAccount>> {

                override fun onSuccess(result: Map<String, FullAccount>) {
                    assertNotNull(result)
                    assertTrue(result.size == 2)
                    assertNotNull(result["1.2.18"])
                }

                override fun onError(error: LocalException) {
                    fail()
                }

            })
    }

    @Test
    fun getFullAccountsErrorTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(null)

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.getFullAccounts(
            listOf("1.2.18", "1.2.19"),
            false,
            object : Callback<Map<String, FullAccount>> {

                override fun onSuccess(result: Map<String, FullAccount>) {
                    fail()
                }

                override fun onError(error: LocalException) {
                    assertNotNull(error)
                }

            })
    }

    @Test
    fun getFullAccountsResultTest() {
        val socketCoreComponent = DatabaseApiServiceSocketCoreComponentMock()

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.getFullAccounts(
            listOf("1.2.18", "1.2.19"),
            false
        ).value { result ->
            assertNotNull(result)
            assertTrue(result.size == 2)
            assertNotNull(result["1.2.18"])
        }.error { fail() }
    }

    @Test
    fun getFullAccountsResultErrorTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(null)

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.getFullAccounts(
            listOf("1.2.18", "1.2.19"),
            false
        )
            .value { fail() }
            .error { error ->
                assertNotNull(error)
                assertTrue(error is LocalException)
            }
    }

    @Test
    fun getChainIdTest() {
        val chainId = "chainId"
        val socketCoreComponent = ServiceSocketCoreComponentMock(chainId)

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.getChainId()
            .value { id ->
                assertNotNull(id)
                assertEquals(id, chainId)
            }
            .error { fail() }
    }

    @Test
    fun getDynamicGlobalPropertiesTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(dynamicGlobalPropertiesResponse)

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.getDynamicGlobalProperties()
            .value { result ->
                assertNotNull(result)
                assertEquals(result, dynamicGlobalPropertiesResponse)
            }
            .error { fail() }
    }

    @Test
    fun getBlockDataTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(dynamicGlobalPropertiesResponse)

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        val blockData = databaseApiService.getBlockData()

        assertNotNull(blockData)
    }

    @Test
    fun getBlockTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(blockResponse)

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.getBlock("1.2.3", object : Callback<Block> {
            override fun onSuccess(result: Block) {
                assertNotNull(result)
                assertEquals(blockResponse, result)
            }

            override fun onError(error: LocalException) {
                fail()
            }

        })
    }

    @Test
    fun getBlockErrorTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(null)

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.getBlock("1.2.3", object : Callback<Block> {
            override fun onSuccess(result: Block) {
                fail()
            }

            override fun onError(error: LocalException) {
                assertNotNull(error)
            }

        })
    }

    @Test
    fun getBlockResultTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(blockResponse)

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.getBlock("1.2.3")
            .value { result ->
                assertNotNull(result)
                assertEquals(blockResponse, result)
            }
            .error { fail() }
    }

    @Test
    fun getBlockResultErrorTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(null)

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.getBlock("1.2.3")
            .value {
                fail()
            }
            .error { error ->
                assertNotNull(error)
            }
    }

    @Test
    fun getRequiredFeesTest() {
        val fees = listOf(
            AssetAmount(UnsignedLong.valueOf(10), Asset("1.3.5")),
            AssetAmount(UnsignedLong.valueOf(5), Asset("1.3.0"))
        )
        val socketCoreComponent = ServiceSocketCoreComponentMock(fees)

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.getRequiredFees(listOf(), Asset("1.3.0"))
            .value { result ->
                assertNotNull(result)
                assertFalse(result.isEmpty())
                assertTrue(result.size == 2)
            }
            .error { fail() }
    }

    @Test
    fun getRequiredFeesErrorTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(null)

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.getRequiredFees(listOf(), Asset("1.3.0"))
            .value { fail() }
            .error { error ->
                assertNotNull(error)
            }
    }


    @Test
    fun callContractNoChangingStateTest() {
        val response = "0000000000000000000000000000000000000000000000000000000000000004"

        val socketCoreComponent = ServiceSocketCoreComponentMock(response)

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.callContractNoChangingState(
            "", "", "", ""
        )
            .value { result ->
                assertNotNull(result)
                assertEquals(result, response)
            }
            .error { fail() }
    }

    @Test
    fun callContractNoChangingStateErrorTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(null)

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.callContractNoChangingState(
            "", "", "", ""
        )
            .value { fail() }
            .error { error ->
                assertNotNull(error)
            }
    }

    @Test
    fun getRegularContractResultTest() {
        val response = ContractResult(
            0,
            "{\"exec_res\":{\"excepted\":\"None\",\"new_address\":\"010000000000000000000000000000000000007c\",\"output\":\"608060405260043610610088576000357c01000000000000000000000000000000000000000000000000000000009004806306fdde031461008d57806318160ddd1461011d57806323b872dd14610148578063313ce567146101db57806370a082311461020c57806395d89b4114610271578063a9059cbb14610301578063dd62ed3e14610374575b600080fd5b34801561009957600080fd5b506100a26103f9565b6040518080602001828103825283818151815260200191508051906020019080838360005b838110156100e25780820151818401526020810190506100c7565b50505050905090810190601f16801561010f5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561012957600080fd5b50610132610497565b6040518082815260200191505060405180910390f35b34801561015457600080fd5b506101c16004803603606081101561016b57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803573ffffffffffffffffffffffffffffffffffffffff1690602001909291908035906020019092919050505061049d565b604051808215151515815260200191505060405180910390f35b3480156101e757600080fd5b506101f06105ca565b604051808260ff1660ff16815260200191505060405180910390f35b34801561021857600080fd5b5061025b6004803603602081101561022f57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff1690602001909291905050506105dd565b6040518082815260200191505060405180910390f35b34801561027d57600080fd5b506102866105f5565b6040518080602001828103825283818151815260200191508051906020019080838360005b838110156102c65780820151818401526020810190506102ab565b50505050905090810190601f1680156102f35780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561030d57600080fd5b5061035a6004803603604081101561032457600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff16906020019092919080359060200190929190505050610693565b604051808215151515815260200191505060405180910390f35b34801561038057600080fd5b506103e36004803603604081101561039757600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803573ffffffffffffffffffffffffffffffffffffffff1690602001909291905050506106aa565b6040518082815260200191505060405180910390f35b60008054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561048f5780601f106104645761010080835404028352916020019161048f565b820191906000526020600020905b81548152906001019060200180831161047257829003601f168201915b505050505081565b60035481565b6000600560008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054821115151561052a57600080fd5b81600560008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600082825403925050819055506105bf8484846106cf565b600190509392505050565b600260009054906101000a900460ff1681565b60046020528060005260406000206000915090505481565b60018054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561068b5780601f106106605761010080835404028352916020019161068b565b820191906000526020600020905b81548152906001019060200180831161066e57829003601f168201915b505050505081565b60006106a03384846106cf565b6001905092915050565b6005602052816000526040600020602052806000526040600020600091509150505481565b600073ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff161415151561070b57600080fd5b80600460008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020541015151561075957600080fd5b600460008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205481600460008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205401101515156107e857600080fd5b6000600460008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054600460008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205401905081600460008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828254039250508190555081600460008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600082825401925050819055508273ffffffffffffffffffffffffffffffffffffffff168473ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef846040518082815260200191505060405180910390a380600460008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054600460008773ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054011415156109f757fe5b5050505056fea165627a7a723058207d7021bb4a32fc7065227e255004298654e202377452c0c4e83d3250f11a38ca0029\",\"code_deposit\":\"Success\",\"gas_refunded\":0,\"gas_for_deposit\":620877,\"deposit_size\":2601},\"tr_receipt\":{\"status_code\":1,\"gas_used\":885323,\"bloom\":\"00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\",\"log\":[]}}"
        )
        val responseRegular = response.toRegular()

        val socketCoreComponent = ServiceSocketCoreComponentMock(response)

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.getContractResult("")
            .value { result ->
                assertNotNull(result)
                assertEquals(result.contractType, ContractType.REGULAR.ordinal)

                val regularResult = result.toRegular()
                assertNotNull(regularResult)

                assertEquals(regularResult!!.execRes.excepted, responseRegular?.execRes?.excepted)
            }
            .error { fail() }
    }

    @Test
    fun getContractResultx86Test() {
        val response = ContractResult(1, "{\"output\":\"\"}")
        val responseX86 = response.toX86()

        val socketCoreComponent = ServiceSocketCoreComponentMock(response)

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.getContractResult("")
            .value { result ->
                assertNotNull(result)
                assertEquals(result.contractType, ContractType.X86.ordinal)

                val result86 = result.toX86()
                assertNotNull(result86)
                assertEquals(result86!!.output, responseX86?.output)
            }
            .error { fail() }
    }

    @Test
    fun getContractResultErrorTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(null)

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.getContractResult("")
            .value { fail() }
            .error { error ->
                assertNotNull(error)
            }
    }

    @Test
    fun getAllContractsTest() {
        val response = listOf(ContractInfo("1.16.1", "2.20.1", false))

        val socketCoreComponent = ServiceSocketCoreComponentMock(response)

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.getAllContracts()
            .value { result ->
                assertNotNull(result)
                assert(result.isNotEmpty())
                assertEquals(result.first().getObjectId(), response.first().getObjectId())
            }
            .error { fail() }
    }

    @Test
    fun getAllContractsErrorTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(null)

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.getAllContracts()
            .value { fail() }
            .error { error ->
                assertNotNull(error)
            }
    }

    @Test
    fun getContractsTest() {
        val response = listOf(ContractInfo("1.16.1", "2.20.1", false))

        val socketCoreComponent = ServiceSocketCoreComponentMock(response)

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.getContracts(listOf())
            .value { result ->
                assertNotNull(result)
                assert(result.isNotEmpty())
                assertEquals(result.first().getObjectId(), response.first().getObjectId())
            }
            .error { fail() }
    }

    @Test
    fun getContractsErrorTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(null)

        val databaseApiService =
            DatabaseApiServiceImpl(
                socketCoreComponent,
                cryptoCorComponnet,
                Echodevnet()
            )

        databaseApiService.getContracts(listOf())
            .value { fail() }
            .error { error ->
                assertNotNull(error)
            }
    }

    @Test
    fun getContractTest() {
        val response = ContractStruct(0, "")

        val socketCoreComponent = ServiceSocketCoreComponentMock(response)

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.getContract("")
            .value { result ->
                assertNotNull(result)
                assertEquals(result.contractType, response.contractType)
            }
            .error { fail() }
    }

    @Test
    fun getContractErrorTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(null)

        val databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, cryptoCorComponnet, Echodevnet())

        databaseApiService.getContract("")
            .value { fail() }
            .error { error ->
                assertNotNull(error)
            }
    }

    /**
     * [SocketCoreComponent] mock for [DatabaseApiService]
     *
     * @author Dmitriy Bushuev
     */
    inner class DatabaseApiServiceSocketCoreComponentMock : SocketCoreComponent {

        override val socketState: SocketState = SocketState.CONNECTED

        override val currentId: Int = 1

        override fun connect(url: String) {
        }

        override fun disconnect() {
        }

        override fun emit(operation: SocketOperation<*>) {
            if (operation is GetAssetsSocketOperation) {
                operation.callback.onSuccess(listOf())
            } else {
                (operation as FullAccountsSocketOperation).callback.onSuccess(fullAccountsResponse)
            }
        }

        override fun on(listener: SocketMessengerListener) {
        }

        override fun off(listener: SocketMessengerListener) {
        }

    }
}
