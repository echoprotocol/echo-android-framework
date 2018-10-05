package org.echo.mobile.framework.model

import com.google.common.primitives.UnsignedLong
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Test cases for [AssetAmount] model
 *
 * @author Dmitriy Bushuev
 */
class AssetAmountTest {

    private lateinit var large: AssetAmount
    private lateinit var small: AssetAmount
    private val testAsset = Asset("1.3.0")

    @Before
    fun setUp() {
        large = AssetAmount(UnsignedLong.valueOf(LARGE_AMOUNT.toLong()), testAsset)
        small = AssetAmount(UnsignedLong.valueOf(SMALL_VALUE.toLong()), testAsset)
    }

    @Test
    fun testAddition() {
        assertEquals(
            (large + small).amount,
            AssetAmount(
                UnsignedLong.valueOf((LARGE_AMOUNT + SMALL_VALUE).toLong()),
                testAsset
            ).amount
        )
    }

    @Test
    fun testSubtraction() {
        assertEquals(
            (large - small).amount,
            AssetAmount(
                UnsignedLong.valueOf((LARGE_AMOUNT - SMALL_VALUE).toLong()),
                testAsset
            ).amount
        )

        assertEquals(
            (small - large).amount,
            AssetAmount(
                UnsignedLong.valueOf(Math.abs(SMALL_VALUE - LARGE_AMOUNT).toLong()),
                testAsset
            ).amount
        )
    }

    @Test
    fun testMultiplication() {
        // Testing a simple multiplication by a double
        val result = large * 0.5
        assertEquals(500, result.amount.toLong())

        // Testing the multiplication of a number that would normally give an overflow
        val max = AssetAmount(UnsignedLong.valueOf(java.lang.Long.MAX_VALUE), testAsset)
        val overMaxLong = max * 1.5
        assertEquals(MULTIPLICATION_TEST_EXPECTED_RESULT, overMaxLong.amount.toString(10))
    }

    @Test
    fun testDivision() {
        // Testing a simple division by a double
        val result = large / 0.5
        assertEquals(2000, result.amount.toLong())

        // Testing a division of a number that would normally give an overflow
        val max = AssetAmount(UnsignedLong.valueOf(java.lang.Long.MAX_VALUE), testAsset)
        val overMaxLong = max / 0.8
        assertEquals(DIVISION_TEST_EXPECTED_RESULT, overMaxLong.amount.toString())
    }

    companion object {
        private const val LARGE_AMOUNT = 1000
        private const val SMALL_VALUE = 500

        private const val MULTIPLICATION_TEST_EXPECTED_RESULT = "13835058055282163712"
        private const val DIVISION_TEST_EXPECTED_RESULT = "11529215046068469760"
    }

}
