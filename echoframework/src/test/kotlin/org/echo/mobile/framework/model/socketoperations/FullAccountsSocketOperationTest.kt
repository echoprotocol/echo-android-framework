package org.echo.mobile.framework.model.socketoperations

import org.echo.mobile.framework.model.network.Testnet
import org.echo.mobile.framework.support.EmptyCallback
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Test cases for [FullAccountsSocketOperation]
 *
 * @author Dmitriy Bushuev
 */
class FullAccountsSocketOperationTest {

    private lateinit var operation: FullAccountsSocketOperation

    @Before
    fun setUp() {
        operation = FullAccountsSocketOperation(
            2,
            listOf("1.2.23215"),
            false,
            Testnet(),
            3,
            callback = EmptyCallback()
        )
    }

    @Test
    fun serializeTest() {
        val json = operation.toJsonObject().asJsonObject

        Assert.assertEquals(json.get(OperationCodingKeys.ID.key).asInt, 3)
        Assert.assertEquals(
            json.get(OperationCodingKeys.METHOD.key).asString,
            SocketMethodType.CALL.key
        )

        val parameters = json.getAsJsonArray(OperationCodingKeys.PARAMS.key)
        Assert.assertTrue(parameters.size() == 3)

        val apiId = parameters[0].asInt
        Assert.assertEquals(apiId, 2)

        val apiName = parameters[1].asString
        Assert.assertEquals(apiName, SocketOperationKeys.FULL_ACCOUNTS.key)
    }

    @Test
    fun deserializeTest() {
        val fullAccountMap = operation.fromJson(RESULT)

        assertNotNull(fullAccountMap)

        val fullAccount = fullAccountMap["daria"]
        assertNotNull(fullAccount)

        val account = fullAccount!!.account
        assertNotNull(account)

        Assert.assertEquals(account!!.getObjectId(), "1.2.16")
    }

    companion object {
        private var RESULT = """{
                                  "id": 6,
                                  "jsonrpc": "2.0",
                                  "result": [
                                    [
                                      "daria",
                                      {
                                        "account": {
                                          "id": "1.2.16",
                                          "membership_expiration_date": "1970-01-01T00:00:00",
                                          "registrar": "1.2.8",
                                          "referrer": "1.2.8",
                                          "lifetime_referrer": "1.2.8",
                                          "network_fee_percentage": 2000,
                                          "lifetime_referrer_fee_percentage": 3000,
                                          "referrer_rewards_percentage": 7500,
                                          "name": "daria",
                                          "active": {
                                            "weight_threshold": 1,
                                            "account_auths": [],
                                            "key_auths": [
                                              [
                                                "DET47zrZrttEpMS3Kva4hnwnGHYhZfzMSjxod898MvLbFae",
                                                1
                                              ]
                                            ]
                                          },
                                          "echorand_key": "DETF4YvRrSmG4sCsyDtScBVKQ3b1iEdsfXhCabLL8ABJi8e",
                                          "options": {
                                            "memo_key": "TEST7X8qQCdssWbVdbwQq6n94m6fP6B7oAWuqZyyNVLnuDQxoBTUfd",
                                            "voting_account": "1.2.5",
                                            "delegating_account": "1.2.8",
                                            "num_committee": 0,
                                            "votes": [],
                                            "extensions": []
                                          },
                                          "statistics": "2.6.16",
                                          "whitelisting_accounts": [],
                                          "blacklisting_accounts": [],
                                          "whitelisted_accounts": [],
                                          "blacklisted_accounts": [],
                                          "owner_special_authority": [
                                            0,
                                            {}
                                          ],
                                          "active_special_authority": [
                                            0,
                                            {}
                                          ],
                                          "top_n_control_flags": 0
                                        },
                                        "statistics": {
                                          "id": "2.6.16",
                                          "owner": "1.2.16",
                                          "most_recent_op": "2.9.22",
                                          "total_ops": 1,
                                          "removed_ops": 0,
                                          "total_core_in_orders": 0,
                                          "lifetime_fees_paid": 0,
                                          "pending_fees": 0,
                                          "pending_vested_fees": 0
                                        },
                                        "registrar_name": "init2",
                                        "referrer_name": "init2",
                                        "lifetime_referrer_name": "init2",
                                        "votes": [],
                                        "balances": [],
                                        "vesting_balances": [],
                                        "limit_orders": [],
                                        "call_orders": [],
                                        "settle_orders": [],
                                        "proposals": [],
                                        "assets": [],
                                        "withdraws": []
                                      }
                                    ]
                                  ]
                                }""".trimMargin()
    }

}
