package com.pixelplex.echolib.model

import com.google.common.math.DoubleMath
import com.google.common.primitives.UnsignedLong
import com.pixelplex.echolib.exception.IncompatibleOperation
import java.math.RoundingMode

/**
 * Class used to represent a specific amount of a certain asset
 *
 * @author Dmitriy Bushuev
 */
class AssetAmount(
    private var amount: UnsignedLong,
    private val asset: Asset
) {

    /**
     * Adds two asset amounts. They must refer to the same Asset type.
     *
     * @param other: The other AssetAmount to add to this.
     * @return: A new instance of the AssetAmount class with the combined amount.
     */
    fun add(other: AssetAmount): AssetAmount {
        checkAssetCompatible(other)

        val combined = amount.plus(other.amount)
        return AssetAmount(combined, asset)
    }

    /**
     * Adds an aditional amount of base units to this AssetAmount.
     *
     * @param additional: The amount to add.
     * @return: A new instance of the AssetAmount class with the added aditional.
     */
    fun add(additional: Long): AssetAmount {
        val combined = amount.plus(UnsignedLong.valueOf(additional))
        return AssetAmount(combined, asset)
    }

    /**
     * Subtracts another instance of AssetAmount from this one. This method will always
     * return absolute values.
     *
     * @param other: The other asset amount to subtract from this.
     * @return: The absolute value of the subtraction of the other minus this asset amount.
     */
    fun substract(other: AssetAmount): AssetAmount {
        checkAssetCompatible(other)

        val result = if (amount < other.amount) {
            other.amount.minus(amount)
        } else {
            amount.minus(other.amount)
        }

        return AssetAmount(result, asset)
    }


    /**
     * Multiplies the current amount by a factor provided as the first parameter. The second parameter
     * specifies the rounding method to be used.
     *
     * @param factor:       The multiplying factor
     * @param roundingMode: The rounding mode as an instance of the {@link RoundingMode} class
     * @return The same AssetAmount instance, but with the changed amount value.
     */
    fun multiplyBy(factor: Double, roundingMode: RoundingMode): AssetAmount {
        this.amount = UnsignedLong.valueOf(
            DoubleMath.roundToLong(
                this.amount.toLong() * factor,
                roundingMode
            )
        )
        return this
    }


    private fun checkAssetCompatible(other: AssetAmount) {
        if (asset.getObjectId() != other.asset.getObjectId()) {
            throw IncompatibleOperation()
        }
    }

}
