package com.pixelplex.echoframework.model.socketoperations

import com.pixelplex.echoframework.model.network.Testnet
import com.pixelplex.echoframework.support.EmptyCallback
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

        val fullAccount = fullAccountMap["dimaty123"]
        assertNotNull(fullAccount)

        val account = fullAccount!!.account
        assertNotNull(account)

        Assert.assertEquals(account!!.getObjectId(), "1.2.23215")
    }

    companion object {
        private var RESULT = """{
                                            |"id":3,
                                            |"jsonrpc":"2.0",
                                            |"result":
                                            |[
                                            |[
                                                |"dimaty123",
                                                |{
                                                |"account":{
                                                |"id":"1.2.23215",
                                                |"membership_expiration_date":"1970-01-01T00:00:00",
                                                |"registrar":"1.2.17",
                                                |"referrer":"1.2.17",
                                                |"lifetime_referrer":"1.2.17",
                                                |"network_fee_percentage":2000,
                                                |"lifetime_referrer_fee_percentage":3000,
                                                |"referrer_rewards_percentage":5000,
                                                |"name":"dimaty123",
                                                |"owner":{
                                                |"weight_threshold":1,
                                                |"account_auths":[
                                                |],
                                                |"key_auths":[
                                                |[
                                                |"TEST6S6ZdMzJxn3kvRHhaK5f3MGELCj76r9yXYhE3CH9NoUDszLtiP",
                                                |1
                                                |]
                                                |],
                                                |"address_auths":[
                                                |]
                                                |},
                                                |"active":{
                                                |"weight_threshold":1,
                                                |"account_auths":[
                                                |],
                                                |"key_auths":[
                                                |[
                                                |"TEST4vUGEra6N7pg9SNSL4SHQPkiXghmKAzDj86TJXEUMPJw1ohHXR",
                                                |1
                                                |]
                                                |],
                                                |"address_auths":[
                                                |]
                                                |},
                                                |"options":{
                                                |"memo_key":"TEST4vUGEra6N7pg9SNSL4SHQPkiXghmKAzDj86TJXEUMPJw1ohHXR",
                                                |"voting_account":"1.2.5",
                                                |"num_witness":0,
                                                |"num_committee":0,
                                                |"votes":[
                                                |],
                                                |"extensions":[
                                                |]
                                                |},
                                                |"statistics":"2.6.23215"
                                                |},
                                                |"registrar_name":"faucet",
                                                |"referrer_name":"faucet",
                                                |"balances":[
                                                |    {
                                                |        "id":"2.5.24777",
                                                |        "owner":"1.2.23215",
                                                |        "asset_type":"1.3.0",
                                                |        "balance":"9825831756"
                                                |    }
                                                |],
                                                |"assets":[
                                                |]
                                                |}
                                            |]
                                            |]
                                            |}
                                        |""".trimMargin()
    }

}
