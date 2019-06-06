package org.echo.mobile.framework.model.operations

import com.google.common.primitives.UnsignedLong
import com.google.gson.GsonBuilder
import org.echo.mobile.bitcoinj.ECKey
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AccountOptions
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.BaseOperation
import org.echo.mobile.framework.model.PublicKey
import org.echo.mobile.framework.model.eddsa.EdAuthority
import org.echo.mobile.framework.model.network.Testnet
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Test cases for [AccountUpdateOperation]
 *
 * @author Dmitriy Bushuev
 */
class AccountUpdateOperationTest {

    @Test
    fun bytesSerializationTest() {
        val operation = buildOperation()

        assertNotNull(operation.toBytes())
    }

    @Test
    fun jsonSerializationTest() {
        val operation = buildOperation()

        val json = operation.toJsonObject().asJsonArray

        Assert.assertTrue(json[0].asByte == OperationType.ACCOUNT_UPDATE_OPERATION.ordinal.toByte())

        val accountUpdateObject = json[1].asJsonObject

        assertNotNull(accountUpdateObject.get(BaseOperation.KEY_FEE))
        assertNotNull(accountUpdateObject.get(AccountUpdateOperation.KEY_ACTIVE))
        assertNotNull(accountUpdateObject.get(AccountUpdateOperation.KEY_ACCOUNT))
        assertNotNull(accountUpdateObject.get(AccountUpdateOperation.KEY_NEW_OPTIONS))
        assertNotNull(accountUpdateObject.get(AccountUpdateOperation.KEY_EXTENSIONS))
    }

    @Test
    fun jsonDeserializationTest() {
        val json = """{
                            "fee":{
                                "amount":13771,
                                "asset_id":"1.3.0"
                            },
                            "account":"1.2.23215",
                            ed_key: "a08fd46ee534e62d08e577a84a28601903d424bdf288be45644ece293672943e",
                            "active":{
                                    "weight_threshold":1,
                                    "account_auths":[
                                ],
                                "key_auths":[
                                    [
                                        "TEST4vUGEra6N7pg9SNSL4SHQPkiXghmKAzDj86TJXEUMPJw1ohHXR",
                                        1
                                    ]
                                ],
                                "address_auths":[
                                ]
                            },
                            "new_options":{
                                "memo_key":"TEST4vUGEra6N7pg9SNSL4SHQPkiXghmKAzDj86TJXEUMPJw1ohHXR",
                                "voting_account":"1.2.5",
                                "delegating_account":"1.2.12",
                                "num_witness":0,
                                "num_committee":0,
                                "votes":[
                                ],
                                "extensions":[
                                ]
                            },
                            "extensions":{
                            }
                        }
                    """.trimMargin()

        val gson = configureGson()

        val transfer =
            gson.fromJson<AccountUpdateOperation>(json, AccountUpdateOperation::class.java)

        Assert.assertTrue(transfer.type == OperationType.ACCOUNT_UPDATE_OPERATION)
        Assert.assertTrue(transfer.fee.amount == UnsignedLong.valueOf(13771))
        Assert.assertTrue(transfer.account.getObjectId() == "1.2.23215")
        Assert.assertTrue(transfer.newOptionsOption.isSet)
        Assert.assertTrue(transfer.activeOption.isSet)
    }

    private fun configureGson() = GsonBuilder().apply {
        registerTypeAdapter(
            AccountUpdateOperation::class.java,
            AccountUpdateOperation.Deserializer()
        )
        registerTypeAdapter(AssetAmount::class.java, AssetAmount.Deserializer())
        registerTypeAdapter(EdAuthority::class.java, EdAuthority.Deserializer())
        registerTypeAdapter(Account::class.java, Account.Deserializer())
        registerTypeAdapter(AccountOptions::class.java, AccountOptions.Deserializer(Testnet()))
    }.create()

    private fun buildOperation(): AccountUpdateOperation {
        val fee = AssetAmount(UnsignedLong.ONE)
        val account = Account("1.2.23215")
        val active = EdAuthority(1)
        val options = AccountOptions()

        return AccountUpdateOperationBuilder()
            .setFee(fee)
            .setAccount(account)
            .setActive(active)
            .setOptions(options)
            .build()
    }

}
