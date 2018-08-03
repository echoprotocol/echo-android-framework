package com.pixelplex.echoframework.service

import com.pixelplex.echoframework.AccountListener
import com.pixelplex.echoframework.model.Account
import com.pixelplex.echoframework.model.network.Echodevnet
import com.pixelplex.echoframework.service.internal.AccountSubscriptionManagerImpl
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test cases for [AccountSubscriptionManagerImpl]
 *
 * @author Dmitriy Bushuev
 */
class AccountSubscriptionManagerTest {

    private val accountSubscriptionManager = AccountSubscriptionManagerImpl(Echodevnet())

    @Test
    fun registeredTest() {
        val registeredId = "1.2.3"
        accountSubscriptionManager.registerListener(registeredId, object : AccountListener {
            override fun onChange(updatedAccount: Account) {
            }
        })

        assertTrue(accountSubscriptionManager.registered(registeredId))
    }

    @Test
    fun removeTest() {
        val registeredId = "1.2.18"
        val listener = object : AccountListener {
            override fun onChange(updatedAccount: Account) {
                Assert.fail()
            }
        }

        accountSubscriptionManager.registerListener(registeredId, listener)
        accountSubscriptionManager.registerListener(registeredId, listener)
        accountSubscriptionManager.registerListener(registeredId, listener)
        accountSubscriptionManager.registerListener(registeredId, listener)

        assertTrue(accountSubscriptionManager.removeListeners(registeredId)!!.size == 4)

        accountSubscriptionManager.processEvent(NOTICE_EVENT)
    }

    @Test
    fun processEventTest() {
        val registeredId = "1.2.18"

        var notifyCount = 0

        val listener = object : AccountListener {
            override fun onChange(updatedAccount: Account) {
                notifyCount++
            }
        }

        accountSubscriptionManager.registerListener(registeredId, listener)
        accountSubscriptionManager.registerListener(registeredId, listener)
        accountSubscriptionManager.registerListener(registeredId, listener)

        accountSubscriptionManager.processEvent(NOTICE_EVENT)

        Assert.assertTrue(notifyCount == 3)
    }

    @Test
    fun notifyTest() {
        val registeredId = "1.2.18"

        var notifyCount = 0

        val listener = object : AccountListener {
            override fun onChange(updatedAccount: Account) {
                notifyCount++
            }
        }

        accountSubscriptionManager.registerListener(registeredId, listener)
        accountSubscriptionManager.registerListener(registeredId, listener)
        accountSubscriptionManager.registerListener(registeredId, listener)

        accountSubscriptionManager.notify(Account("1.2.18"))

        Assert.assertTrue(notifyCount == 3)
    }

    companion object {
        private var NOTICE_EVENT = """  |{
                                                  |"method":"notice",
                                                  |"params":
                                                  |[
                                                       |4,
                                                       |[
                                                          |[
                                                              |{
                                                                  |"id":"2.1.0"
                                                              |},
                                                              |{
                                                                  |"id":"2.8.25373"
                                                              |},
                                                              |{
                                                                  |"id":"2.5.24777",
                                                                  |"owner":"1.2.23215"
                                                              |},
                                                              |{
                                                                  |"id":"1.2.18",
                                                                  |"membership_expiration_date":"1970-01-01T00:00:00",
                                                                  |"registrar":"1.2.17",
                                                                  |"referrer":"1.2.17",
                                                                  |"lifetime_referrer":"1.2.17",
                                                                  |"network_fee_percentage":2000,
                                                                  |"lifetime_referrer_fee_percentage":3000,
                                                                  |"referrer_rewards_percentage":0,
                                                                  |"name":"dima1",
                                                                  |"owner":{
                                                                      |"weight_threshold":1,
                                                                      |"account_auths":[
                                                                    |],
                                                                    |"key_auths":[
                                                                      |[
                                                                          |"ECHO5qrbEeNrHWvGKwx5XVhLCNoytYFhRpSybQT7qpc3SBboEPCEvB",
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
                                                                            |"ECHO7ehHRwcqQ8FTtiVfHpb6FHNDF34QrV8W1Z3uTL5izF7bKAJ6YW",
                                                                            |1
                                                                         |]
                                                                    |],
                                                                    |"address_auths":[
                                                                    |]
                                                                  |},
                                                                  |"options":{
                                                                  |"memo_key":"ECHO7ehHRwcqQ8FTtiVfHpb6FHNDF34QrV8W1Z3uTL5izF7bKAJ6YW",
                                                                  |"voting_account":"1.2.5",
                                                                  |"num_witness":0,
                                                                  |"num_committee":0,
                                                                  |"votes":[
                                                                  |],
                                                                  |"extensions":[
                                                                  |]
                                                                  |},
                                                                  |"statistics":"2.6.18",
                                                                  |"whitelisting_accounts":[
                                                                  |],
                                                                  |"blacklisting_accounts":[
                                                                  |],
                                                                  |"whitelisted_accounts":[
                                                                  |],
                                                                  |"blacklisted_accounts":[
                                                                  |],
                                                                  |"owner_special_authority":[
                                                                  |0,
                                                                  |{
                                                                  |}
                                                                  |],
                                                                  |"active_special_authority":[
                                                                  |0,
                                                                  |{
                                                                  |}
                                                                  |],
                                                                  |"top_n_control_flags":0
                                                                  |}
                                                          |]
                                                       |]
                                                      |]
                                                      |}""".trimMargin()
    }

}
