package org.echo.mobile.framework.support

import java.math.BigDecimal

/**
 * Provides fee [ratio] value
 *
 * @author Daria Pechkovskaya
 */
class FeeRatioProvider(private val ratio: BigDecimal) : Provider<Double> {

    override fun provide(): Double =
        try {
            ratio.toDouble()
        } catch (e: Exception) {
            BigDecimal.ONE.toDouble()
        }
}