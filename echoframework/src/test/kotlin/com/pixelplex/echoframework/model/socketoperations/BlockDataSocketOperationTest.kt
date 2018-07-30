package com.pixelplex.echoframework.model.socketoperations

import com.pixelplex.echoframework.support.EmptyCallback
import com.pixelplex.echoframework.support.parse
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Test cases for [BlockDataSocketOperation]
 *
 * @author Dmitriy Bushuev
 */
class BlockDataSocketOperationTest {

    private lateinit var operation: BlockDataSocketOperation

    @Before
    fun setUp() {
        operation =
                BlockDataSocketOperation(
                    2,
                    callback = EmptyCallback()
                )
    }

    @Test
    fun serializeTest() {
        operation.callId = 3
        val json = operation.toJsonObject().asJsonObject

        assertEquals(json.get(OperationCodingKeys.ID.key).asInt, 3)
        assertEquals(
            json.get(OperationCodingKeys.METHOD.key).asString,
            SocketMethodType.CALL.key
        )

        val parameters = json.getAsJsonArray(OperationCodingKeys.PARAMS.key)
        Assert.assertTrue(parameters.size() == 3)

        val apiId = parameters[0].asInt
        assertEquals(apiId, 2)

        val apiName = parameters[1].asString
        assertEquals(apiName, SocketOperationKeys.BLOCK_DATA.key)
    }

    @Test
    fun deserializeTest() {
        val block = operation.fromJson(RESULT)

        assertNotNull(block)
        assertEquals(block!!.getObjectId(), "2.1.0")

        val date = "2018-07-30T09:44:33".parse()

        assertEquals(date, block.date)
    }

    companion object {
        private var RESULT = """{"id":6,
                                        |"jsonrpc":"2.0",
                                        |"result":
                                            |{"id":"2.1.0",
                                            |"head_block_number":19328276,
                                            |"head_block_id":"0126ed14e93d900ef738667062fe835528cc67ea",
                                            |"time":"2018-07-30T09:44:33",
                                            |"current_witness":"1.6.54",
                                            |"next_maintenance_time":"2018-07-30T09:46:00",
                                            |"last_budget_time":"2018-07-30T09:41:00",
                                            |"witness_budget":0,
                                            |"accounts_registered_this_interval":0,
                                            |"recently_missed_count":0,
                                            |"current_aslot":20945484,
                                            |"recent_slots_filled":"339596922878479421610317445280965849039",
                                            |"dynamic_flags":0,
                                            |"last_irreversible_block_num":19328250}}
                                        |""".trimMargin()
    }

}
