package com.pixelplex.echolib.model

import com.google.common.math.DoubleMath
import com.google.common.primitives.UnsignedLong
import com.pixelplex.echolib.exception.IncompatibleOperationException
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Class used to represent a specific amount of a certain asset
 *
 * @author Dmitriy Bushuev
 * @author Dasha Pechkovskaya
 */
class AssetAmount(
    var amount: UnsignedLong,
    val asset: Asset
) {

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

    companion object {
        private const val DEFAULT_SCALE = 18
    }

}
