package com.pixelplex.echoframework.model

import com.google.common.math.DoubleMath
import com.google.common.primitives.UnsignedLong
import com.google.gson.*
import com.pixelplex.bitcoinj.revert
import com.pixelplex.echoframework.ECHO_ASSET_ID
import com.pixelplex.echoframework.exception.IncompatibleOperationException
import com.pixelplex.echoframework.support.toUnsignedByteArray
import java.lang.reflect.Type
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Class used to represent a specific amount of a certain asset
 *
 * @author Dmitriy Bushuev
 * @author Dasha Pechkovskaya
 */
class AssetAmount @JvmOverloads constructor(
    var amount: UnsignedLong,
    val asset: Asset = Asset(ECHO_ASSET_ID)
) : GrapheneSerializable {

    /**
     * Adds two asset amounts. They must refer to the same Asset type.
     *
     * @param other: The other AssetAmount to add to this.
     * @return:      A new instance of the AssetAmount class with the combined amount.
     */
    operator fun plus(other: AssetAmount): AssetAmount {
        checkAssetCompatible(other)

        val combined = amount.plus(other.amount)
        return AssetAmount(combined, asset)
    }

    /**
     * Adds an aditional amount of base units to this AssetAmount.
     *
     * @param additional: The amount to add.
     * @return:           A new instance of the AssetAmount class with the added aditional.
     */
    operator fun plus(additional: Long): AssetAmount {
        val combined = amount.plus(UnsignedLong.valueOf(additional))
        return AssetAmount(combined, asset)
    }

    /**
     * Subtracts another instance of AssetAmount from this one. This method will always
     * return absolute values.
     *
     * @param other: The other asset amount to subtract from this.
     * @return:      The absolute value of the subtraction of the other minus this asset amount.
     */
    operator fun minus(other: AssetAmount): AssetAmount {
        checkAssetCompatible(other)

        val result = if (amount < other.amount) {
            other.amount.minus(amount)
        } else {
            amount.minus(other.amount)
        }

        return AssetAmount(result, asset)
    }

    /**
     * Divides the current amount by a divisor provided as the first parameter, using
     * the [RoundingMode.HALF_DOWN] constant
     *
     * @param divisor: The divisor
     * @return:        The same AssetAMount instance, but with the divided amount value
     */
    operator fun div(divisor: Double): AssetAmount = divideBy(divisor, RoundingMode.HALF_DOWN)

    /**
     * Multiplies the current amount by a factor, using the {@link RoundingMode#HALF_DOWN} constant.
     *
     * @param factor: The multiplying factor
     * @return        The same AssetAmount instance, but with the changed amount value.
     */
    operator fun times(factor: Double): AssetAmount = multiplyBy(factor, RoundingMode.HALF_DOWN)

    /**
     * Multiplies the current amount by a factor provided as the first parameter. The second parameter
     * specifies the rounding method to be used.
     *
     * @param factor:       The multiplying factor
     * @param roundingMode: The rounding mode as an instance of the {@link RoundingMode} class
     * @return              The same AssetAmount instance, but with the changed amount value.
     */
    fun multiplyBy(factor: Double, roundingMode: RoundingMode): AssetAmount {
        val originalAmount = BigDecimal(amount.bigIntegerValue())
        val decimalResult = originalAmount.multiply(BigDecimal(factor))
        val resultingAmount = UnsignedLong.valueOf(
            DoubleMath.roundToBigInteger(
                decimalResult.toDouble(),
                roundingMode
            )
        )

        return AssetAmount(resultingAmount, Asset(asset))
    }

    /**
     * Divides the current amount by a divisor provided as the first parameter. The second parameter
     * specifies the rounding method to be used.
     *
     * @param divisor: The divisor
     * @return:        The same AssetAMount instance, but with the divided amount value
     */
    fun divideBy(divisor: Double, roundingMode: RoundingMode): AssetAmount {
        val originalAmount = BigDecimal(amount.bigIntegerValue())
        val decimalAmount =
            originalAmount.divide(BigDecimal(divisor), DEFAULT_SCALE, RoundingMode.HALF_UP)
        val resultingAmount = UnsignedLong.valueOf(
            DoubleMath.roundToBigInteger(
                decimalAmount.toDouble(),
                roundingMode
            )
        )
        return AssetAmount(resultingAmount, Asset(asset))
    }

    private fun checkAssetCompatible(other: AssetAmount) {
        if (asset.getObjectId() != other.asset.getObjectId()) {
            throw IncompatibleOperationException(
                "Cannot add two AssetAmount instances that refer to different assets"
            )
        }
    }

    override fun toBytes(): ByteArray {
        val assetId = asset.instance.toUnsignedByteArray()
        val value = this.amount.toLong().revert()

        return value + assetId
    }

    override fun toJsonString(): String? {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(AssetAmount::class.java, Serializer())
        return gsonBuilder.create().toJson(this)
    }

    override fun toJsonObject(): JsonElement? = JsonObject().apply {
        addProperty(KEY_AMOUNT, amount)
        addProperty(KEY_ASSET_ID, asset.getObjectId())
    }

    /**
     * Custom serializer used to translate this object into the JSON-formatted entry we need for a transaction.
     */
    class Serializer : JsonSerializer<AssetAmount> {

        override fun serialize(
            assetAmount: AssetAmount?,
            type: Type?,
            jsonSerializationContext: JsonSerializationContext?
        ): JsonElement? = assetAmount?.toJsonObject()
    }

    /**
     * Custom deserializer used for this class
     */
    class Deserializer : JsonDeserializer<AssetAmount> {

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            type: Type,
            jsonDeserializationContext: JsonDeserializationContext
        ): AssetAmount {
            val amount = json.asJsonObject.get(KEY_AMOUNT).asLong
            val assetId = json.asJsonObject.get(KEY_ASSET_ID).asString
            return AssetAmount(UnsignedLong.valueOf(amount), Asset(assetId))
        }
    }

    companion object {
        private const val DEFAULT_SCALE = 18

        private const val KEY_AMOUNT = "amount"
        private const val KEY_ASSET_ID = "asset_id"
    }

}
