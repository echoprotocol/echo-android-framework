package org.echo.mobile.framework.model

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import org.echo.mobile.framework.model.operations.OperationType

/**
 * Represents base operation model in Graphene blockchain
 *
 * @author Daria Pechkovskaya
 */
abstract class BaseOperation(var type: OperationType) : ByteSerializable,
    JsonSerializable {

    protected var extensions: Extensions = Extensions()

    val id: Byte
        get() = type.ordinal.toByte()

    override fun toJsonObject(): JsonElement =
        JsonArray().apply {
            add(id)
        }

    /**
     * Required amount of fee [AssetAmount] to operation
     */
    abstract var fee: AssetAmount


    override fun toString(): String = "${javaClass.simpleName}(${toJsonObject()})"

    companion object {
        const val KEY_FEE = "fee"
    }

}
