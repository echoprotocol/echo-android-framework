package org.echo.mobile.framework.model.contract

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import org.echo.mobile.framework.model.GrapheneObject
import java.lang.reflect.Type

/**
 * Represents contract balance model from blockchain
 *
 * @author Daria Pechkovskaya
 */
class ContractBalance(id: String) : GrapheneObject(id) {

    var contract: Contract? = null

    var operationId: String? = null

    var sequence: String? = null

    var parentOpId: String? = null

    var next: ContractBalance? = null

    /**
     * Json deserializer for [ContractBalance] class.
     * Deserialize by example:
     * {
     *     "id": "2.16.28",
     *     "contract": "1.14.1",
     *     "operation_id": "1.10.81",
     *     "sequence": 5,
     *     "next": "2.16.19"
     * }
     */
    class Deserializer : JsonDeserializer<ContractBalance> {

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): ContractBalance? {
            if (json == null || !json.isJsonObject) return null

            val jsonOptions = json.asJsonObject

            val id = jsonOptions.get(KEY_ID).asString

            return ContractBalance(id).apply {
                contract = Contract(jsonOptions.get(KEY_CONTRACT).asString)
                operationId = jsonOptions.get(KEY_OPERATION_ID).asString
                sequence = jsonOptions.get(KEY_SEQUENCE).asString
                parentOpId = jsonOptions.get(KEY_PARENT_OPERATION_ID)?.asString
                next = ContractBalance(jsonOptions.get(KEY_NEXT).asString)
            }
        }
    }

    companion object {
        const val KEY_CONTRACT = "contract"
        const val KEY_OPERATION_ID = "operation_id"
        const val KEY_PARENT_OPERATION_ID = "parent_op_id"
        const val KEY_SEQUENCE = "sequence"
        const val KEY_NEXT = "next"
    }
}