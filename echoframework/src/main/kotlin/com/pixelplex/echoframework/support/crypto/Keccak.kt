package com.pixelplex.echoframework.support.crypto

import org.spongycastle.util.Arrays.reverse
import org.spongycastle.util.encoders.Hex
import java.math.BigInteger

/**
 * Keccak encryption implementation.
 *
 * @author Daria Pechkovskaya
 */
class Keccak {

    /**
     * round constants rc
     */
    private val rc = arrayOf(
        BigInteger("0000000000000001", 16),
        BigInteger("0000000000008082", 16),
        BigInteger("800000000000808A", 16),
        BigInteger("8000000080008000", 16),
        BigInteger("000000000000808B", 16),
        BigInteger("0000000080000001", 16),
        BigInteger("8000000080008081", 16),
        BigInteger("8000000000008009", 16),
        BigInteger("000000000000008A", 16),
        BigInteger("0000000000000088", 16),
        BigInteger("0000000080008009", 16),
        BigInteger("000000008000000A", 16),
        BigInteger("000000008000808B", 16),
        BigInteger("800000000000008B", 16),
        BigInteger("8000000000008089", 16),
        BigInteger("8000000000008003", 16),
        BigInteger("8000000000008002", 16),
        BigInteger("8000000000000080", 16),
        BigInteger("000000000000800A", 16),
        BigInteger("800000008000000A", 16),
        BigInteger("8000000080008081", 16),
        BigInteger("8000000000008080", 16),
        BigInteger("0000000080000001", 16),
        BigInteger("8000000080008008", 16)
    )

    //	The rotation offsets r[x,y].
    private val r = arrayOf(
        intArrayOf(0, 36, 3, 41, 18),
        intArrayOf(1, 44, 10, 45, 2),
        intArrayOf(62, 6, 43, 15, 61),
        intArrayOf(28, 55, 25, 21, 56),
        intArrayOf(27, 20, 39, 8, 14)
    )

    private var w: Int = 0

    private var n: Int = 0

    constructor() {
        initialize(DEFAULT_PERMUTATION_WIDTH)
    }

    /**
     * Constructor
     *
     * @param b {25, 50, 100, 200, 400, 800, 1600} sha-3 -> b = 1600
     */
    constructor(b: Int) {
        initialize(b)
    }

    /**
     * Encrypts message by parameter
     *
     * @param message Message to encrypting
     * @param parameter Parameter of encryption
     *
     *
     * @return Encrypted message string
     */
    fun getHash(message: String, parameter: Parameter): String {
        //		Initialization and padding
        val s = Array<Array<BigInteger?>>(5) { arrayOfNulls(5) }

        for (i in 0..4) {
            for (j in 0..4) {
                s[i][j] = BigInteger("0", 16)
            }
        }

        val p = padding(message, parameter)

        //	    Absorbing phase
        for (Pi in p) {
            for (i in 0..4) {
                for (j in 0..4) {
                    if (i + j * 5 < parameter.r / w) {
                        s[i][j] = s[i][j]?.xor(Pi!![i + j * 5])
                    }
                }
            }

            doKeccackf(s)
        }

        //	    Squeezing phase
        var z = ""

        do {

            for (i in 0..4) {
                for (j in 0..4) {
                    if (5 * i + j < parameter.r / w) {
                        z += addZero(
                            Hex.toHexString(reverse(s[j][i]?.toByteArray())),
                            16
                        ).substring(0, 16)
                    }
                }
            }

            doKeccackf(s)
        } while (z.length < parameter.outputLength * 2)

        return z.substring(0, parameter.outputLength * 2)
    }

    private fun doKeccackf(A: Array<Array<BigInteger?>>): Array<Array<BigInteger?>> {
        var a = A
        for (i in 0 until n) {
            a = roundB(a, rc[i])
        }
        return a
    }

    private fun roundB(A: Array<Array<BigInteger?>>, RC: BigInteger): Array<Array<BigInteger?>> {
        val c = arrayOfNulls<BigInteger>(5)
        val d = arrayOfNulls<BigInteger>(5)
        val b = Array<Array<BigInteger?>>(5) { arrayOfNulls(5) }

        //θ step
        for (i in 0..4) {
            c[i] = A[i][0]!!.xor(A[i][1]).xor(A[i][2]).xor(A[i][3]).xor(A[i][4])
        }

        for (i in 0..4) {
            d[i] = c[(i + 4) % 5]?.xor(c[(i + 1) % 5]?.let { rot(it, 1) })
        }

        for (i in 0..4) {
            for (j in 0..4) {
                A[i][j] = A[i][j]!!.xor(d[i])
            }
        }

        //ρ and π steps
        for (i in 0..4) {
            for (j in 0..4) {
                b[j][(2 * i + 3 * j) % 5] = A[i][j]?.let { rot(it, r[i][j]) }
            }
        }

        //χ step
        for (i in 0..4) {
            for (j in 0..4) {
                A[i][j] = b[i][j]!!.xor(b[(i + 1) % 5][j]!!.not().and(b[(i + 2) % 5][j]))
            }
        }

        //ι step
        A[0][0] = A[0][0]!!.xor(RC)

        return A
    }

    private fun rot(x: BigInteger, N: Int): BigInteger {
        var n = N
        n %= w

        val leftShift = getShiftLeft64(x, n)
        val rightShift = x.shiftRight(w - n)

        return leftShift.or(rightShift)
    }

    private fun getShiftLeft64(value: BigInteger, shift: Int): BigInteger {
        var retValue = value.shiftLeft(shift)
        var tmpValue = value.shiftLeft(shift)

        if (retValue > bit64) {
            for (i in 64 until 64 + shift) {
                tmpValue = tmpValue.clearBit(i)
            }

            tmpValue = tmpValue.setBit(64 + shift)
            retValue = tmpValue.and(retValue)
        }
        return retValue
    }

    private fun padding(message: String, parameters: Parameter): Array<Array<BigInteger?>?> {
        var newMessage = message
        val size: Int
        newMessage += parameters.d

        while (newMessage.length / 2 * 8 % parameters.r != parameters.r - 8) {
            newMessage += "00"
        }

        newMessage += "80"
        size = newMessage.length / 2 * 8 / parameters.r

        val arrayM = arrayOfNulls<Array<BigInteger?>>(size)
        arrayM[0] = arrayOfNulls(1600 / w)
        initArray(arrayM[0])

        var count = 0
        var j = 0
        var i = 0

        for (_n in 0 until newMessage.length) {

            if (j > parameters.r / w - 1) {
                j = 0
                i++
                arrayM[i] = arrayOfNulls(1600 / w)
                initArray(arrayM[i])
            }

            count++

            if (count * 4 % w == 0) {
                val subString = newMessage.substring(count - w / 4, w / 4 + (count - w / 4))
                arrayM[i]!![j] = BigInteger(subString, 16)
                var revertString = Hex.toHexString(reverse(arrayM[i]!![j]!!.toByteArray()))
                revertString = addZero(revertString, subString.length)
                arrayM[i]!![j] = BigInteger(revertString, 16)
                j++
            }

        }
        return arrayM
    }

    private fun addZero(str: String, length: Int): String {
        var retStr = str
        for (i in 0 until length - str.length) {
            retStr += "0"
        }
        return retStr
    }

    private fun initArray(array: Array<BigInteger?>?) {
        if (array != null) {
            for (i in array.indices) {
                array[i] = BigInteger("0", 16)
            }
        }
    }

    private fun initialize(b: Int) {
        w = b / 25
        val l = (Math.log(w.toDouble()) / Math.log(2.0)).toInt()
        n = 12 + 2 * l
    }

    companion object {

        private const val DEFAULT_PERMUTATION_WIDTH = 1600

        /**
         * max unsigned long
         */
        private val bit64 = BigInteger("18446744073709551615")
    }
}
