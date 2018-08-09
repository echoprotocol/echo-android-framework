package com.pixelplex.echoframework.service

import com.google.common.primitives.UnsignedLong
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.model.*
import com.pixelplex.echoframework.model.contract.*
import com.pixelplex.echoframework.model.network.Echodevnet
import com.pixelplex.echoframework.service.internal.DatabaseApiServiceImpl
import com.pixelplex.echoframework.support.error
import com.pixelplex.echoframework.support.value
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * Test cases for [DatabaseApiServiceImpl]
 *
 * @author Dmitriy Bushuev
 */
class DatabaseApiServiceTest {

    private lateinit var fullAccountsResponse: Map<String, FullAccount>
    private lateinit var dynamicGlobalPropertiesResponse: DynamicGlobalProperties
    private lateinit var blockResponse: Block

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
        val socketCoreComponent = ServiceSocketCoreComponentMock(fullAccountsResponse)

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

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

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

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
        val socketCoreComponent = ServiceSocketCoreComponentMock(fullAccountsResponse)

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

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

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

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

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

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

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

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

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

        val blockData = databaseApiService.getBlockData()

        assertNotNull(blockData)
    }

    @Test
    fun getBlockTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(blockResponse)

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

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

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

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

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

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

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

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

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

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

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

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

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

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

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

        databaseApiService.callContractNoChangingState(
            "", "", "", ""
        )
            .value { fail() }
            .error { error ->
                assertNotNull(error)
            }
    }

    @Test
    fun getContractResultTest() {
        val response = ContractResult(ExecRes(excepted = "Done"), TrReceipt())

        val socketCoreComponent = ServiceSocketCoreComponentMock(response)

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

        databaseApiService.getContractResult("")
            .value { result ->
                assertNotNull(result)
                assertEquals(result.execRes.excepted, response.execRes.excepted)
            }
            .error { fail() }
    }

    @Test
    fun getContractResultErrorTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(null)

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

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

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

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

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

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

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

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

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

        databaseApiService.getContracts(listOf())
            .value { fail() }
            .error { error ->
                assertNotNull(error)
            }
    }

    @Test
    fun getContractTest() {
        val response = ContractStruct(ContractInfo("1.16.1", "2.20.1", false))

        val socketCoreComponent = ServiceSocketCoreComponentMock(response)

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

        databaseApiService.getContract("")
            .value { result ->
                assertNotNull(result)
                assertEquals(result.contractInfo.getObjectId(), result.contractInfo.getObjectId())
            }
            .error { fail() }
    }

    @Test
    fun getContractErrorTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(null)

        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, Echodevnet())

        databaseApiService.getContract("")
            .value { fail() }
            .error { error ->
                assertNotNull(error)
            }
    }
}
