/*
 * Copyright by the original author or authors.
 * Copyright 2018 Bushuev Dmitriy and Dasha Pechkovskaya
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pixelplex.bitcoinj

import org.spongycastle.math.ec.ECCurve
import org.spongycastle.math.ec.ECFieldElement
import org.spongycastle.math.ec.ECPoint
import java.math.BigInteger
import java.util.*

/**
 * A wrapper around ECPoint that delays decoding of the point for as long as possible. This is useful because point
 * encode/decode in Bouncy Castle is quite slow especially on Dalvik, as it often involves decompression/recompression.
 *
 * @author Daria Pechkovskaya
 */
class LazyECPoint {

    // If curve is set, bits is also set. If curve is unset, point is set and bits is unset. Point can be set along
    // with curve and bits when the cached form has been accessed and thus must have been converted.
    private val curve: ECCurve?
    private val bits: ByteArray?

    // This field is effectively final - once set it won't change again. However it can be set after
    // construction.
    private var point: ECPoint? = null

    // Delegated methods.

    val detachedPoint: ECPoint
        get() = get().detachedPoint

    val encoded: ByteArray
        get() = if (bits != null)
            Arrays.copyOf(bits, bits.size)
        else
            get().encoded

    val isInfinity: Boolean
        get() = get().isInfinity

    val yCoord: ECFieldElement
        get() = get().yCoord

    val zCoords: Array<ECFieldElement>
        get() = get().zCoords

    val isNormalized: Boolean
        get() = get().isNormalized

    val isCompressed: Boolean
        get() = if (bits != null)
            bits[0].toInt() == 2 || bits[0].toInt() == 3
        else
            get().isCompressed

    val isValid: Boolean
        get() = get().isValid

    val xCoord: ECFieldElement
        get() = get().xCoord

    val y: ECFieldElement
        get() = this.normalize().yCoord

    val affineYCoord: ECFieldElement
        get() = get().affineYCoord

    val affineXCoord: ECFieldElement
        get() = get().affineXCoord

    val x: ECFieldElement
        get() = this.normalize().xCoord

    private val canonicalEncoding: ByteArray
        get() = getEncoded(true)

    constructor(curve: ECCurve, bits: ByteArray) {
        this.curve = curve
        this.bits = bits
    }

    constructor(point: ECPoint) {
        this.point = point
        this.curve = null
        this.bits = null
    }

    /**
     * Returns point, created from specified bits
     */
    fun get(): ECPoint {
        if (point == null) {
            point = curve!!.decodePoint(bits!!)
        }
        return point!!
    }

    /**
     * Doubles current point
     */
    fun timesPow2(e: Int): ECPoint = get().timesPow2(e)

    /**
     * Multiplies current point by [k]
     */
    fun multiply(k: BigInteger): ECPoint = get().multiply(k)

    /**
     * Subtracts [b] from current point
     */
    fun subtract(b: ECPoint): ECPoint = get().subtract(b)

    /**
     * Scales y part of point with [scale]
     */
    fun scaleY(scale: ECFieldElement): ECPoint = get().scaleY(scale)

    /**
     * Scales x part of point with [scale]
     */
    fun scaleX(scale: ECFieldElement): ECPoint = get().scaleX(scale)

    /**
     * Check equality of two point
     */
    fun equals(other: ECPoint) = get().equals(other)

    /**
     * Denies current EcPint
     */
    fun negate(): ECPoint = get().negate()

    /**
     * Multiply current point by three
     */
    fun threeTimes(): ECPoint = get().threeTimes()

    /**
     * Returns z coordinate of current point
     */
    fun getZCoord(index: Int): ECFieldElement = get().getZCoord(index)

    /**
     * Returns the field element encoded with point compression
     */
    fun getEncoded(compressed: Boolean): ByteArray =
        if (compressed == isCompressed && bits != null)
            Arrays.copyOf(bits, bits.size)
        else
            get().getEncoded(compressed)

    /**
     * Add [b] to current point
     */
    fun add(b: ECPoint): ECPoint = get().add(b)

    /**
     * Doubles current point and add [b]
     */
    fun twicePlus(b: ECPoint): ECPoint = get().twicePlus(b)

    /**
     * Returns curve associated with current point
     */
    fun getCurve(): ECCurve = get().curve

    /**
     * Normalization ensures that any projective coordinate is 1, and therefore that the x, y
     * coordinates reflect those of the equivalent point in an affine coordinate system.
     *
     * @return a new ECPoint instance representing the same point, but with normalized coordinates
     */
    fun normalize(): ECPoint = get().normalize()

    /**
     * Doubles current point
     */
    fun twice(): ECPoint = get().twice()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return if (other == null || javaClass != other.javaClass)
            false
        else Arrays.equals(
            canonicalEncoding,
            (other as LazyECPoint).canonicalEncoding
        )
    }

    override fun hashCode() = Arrays.hashCode(canonicalEncoding)

}
