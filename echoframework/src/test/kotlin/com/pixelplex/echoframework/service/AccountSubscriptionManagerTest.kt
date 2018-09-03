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
    }

    @Test
    fun processEventTest() {
        val registeredId = "1.2.18"

        val listener = object : AccountListener {
            override fun onChange(updatedAccount: Account) {
            }
        }

        accountSubscriptionManager.registerListener(registeredId, listener)

        val ids = accountSubscriptionManager.processEvent(NOTICE_EVENT)

        Assert.assertEquals(1, ids.size)
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
        private var NOTICE_EVENT = """{
                                                        |"method":"notice",
                                                            |"params":[
                                                                |5,[
                                                                    |[
                                                                        {
                                                                            "id":"2.5.2",
                                                                            "owner":"1.2.18",
                                                                            "asset_type":"1.3.0",
                                                                            "balance":73353484
                                                                        },
                                                                        {
                                                                            "id":"2.6.18",
                                                                            "owner":"1.2.18",
                                                                            "most_recent_op":"2.9.45873",
                                                                            "total_ops":312,
                                                                            "removed_ops":0,
                                                                            "total_core_in_orders":0,
                                                                            "lifetime_fees_paid":289921097,
                                                                            "pending_fees":0,
                                                                            "pending_vested_fees":26175448
                                                                        },
                                                                        {
                                                                            "id":"2.1.0",
                                                                            "head_block_number":317096,
                                                                            "head_block_id":"0004d6a8370560c50481b8fb412e3b9b8863eaf0",
                                                                            "time":"2018-08-28T10:45:50",
                                                                            "current_witness":"1.6.10",
                                                                            "next_maintenance_time":"2018-08-29T00:00:00",
                                                                            "last_budget_time":"2018-08-28T00:00:00",
                                                                            "witness_budget":0,
                                                                            "accounts_registered_this_interval":4,
                                                                            "recently_missed_count":0,
                                                                            "current_aslot":5202344,
                                                                            "recent_slots_filled":"340282366920938463463374607431768211455",
                                                                            "dynamic_flags":0,
                                                                            "last_irreversible_block_num":317080
                                                                        }
                                                                    |]
                                                                |]
                                                            |]
                                                        }""".trimMargin()
    }

}
