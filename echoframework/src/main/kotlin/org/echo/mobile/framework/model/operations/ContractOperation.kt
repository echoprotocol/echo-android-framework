package org.echo.mobile.framework.model.operations

import com.google.common.primitives.UnsignedLong
import com.google.gson.GsonBuilder
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.BaseOperation
import org.echo.mobile.framework.model.contract.ContractResult

/**
 * Base implementation of contract operation
 *
 * @author Dmitriy Bushuev
 */
abstract class ContractOperation @JvmOverloads constructor(
    var registrar: Account,
    val value: AssetAmount,
    val gasPrice: UnsignedLong,
    val gas: UnsignedLong,
    val code: String,
    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO),
    operationType: OperationType
) : BaseOperation(operationType) {

    var contractResult: ContractResult? = null

    override fun toJsonString(): String? {
        val gson = GsonBuilder()
            .registerTypeAdapter(
                ContractCallOperation::class.java,
                ContractCallOperation.Serializer()
            )
            .registerTypeAdapter(
                ContractCreateOperation::class.java,
                ContractCreateOperation.Serializer()
            )
            .create()
        return gson.toJson(this)
    }

    override fun toString(): String = "${javaClass.simpleName}(${toJsonObject()})"

    companion object {
        const val KEY_REGISTRAR = "registrar"
        const val KEY_RECEIVER = "callee"
        const val KEY_VALUE = "value"
        const val KEY_GAS_PRICE = "gasPrice"
        const val KEY_GAS = "gas"
        const val KEY_CODE = "code"
        const val KEY_ACCURACY = "eth_accuracy"
        const val KEY_SUPPORTED_ASSET = "supported_asset_id"
    }

}
