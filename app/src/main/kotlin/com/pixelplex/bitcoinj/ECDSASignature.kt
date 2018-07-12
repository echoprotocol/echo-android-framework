/*
 * Copyright 2011 Google Inc.
 * Copyright 2014 Andreas Schildbach
 * Copyright 2014-2016 the libsecp256k1 contributors
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

import com.google.common.base.Objects
import java.math.BigInteger

/**
 * Groups the two components that make up a signature, and provides a way to encode to DER form, which is
 * how ECDSA signatures are represented when embedded in other data structures in the Bitcoin protocol. The raw
 * components can be useful for doing further EC maths on them.
 *
 * @author Dmitriy Bushuev
 */
class ECDSASignature
/**
 * Constructs a signature with the given components. Does NOT automatically canonicalise the signature.
 * @params The two components of the signature.
 */
constructor(val r: BigInteger, val s: BigInteger) {

    /**
     * Returns true if the S component is "low", that means it is below [ECKey.HALF_CURVE_ORDER].
     * See [BIP62](https://github.com/bitcoin/bips/blob/master/bip-0062.mediawiki#Low_S_values_in_signatures).
     */
    val isCanonical: Boolean
        get() = s <= ECKey.HALF_CURVE_ORDER

    /**
     * Will automatically adjust the S component to be less than or equal to half the curve order, if necessary.
     * This is required because for every signature (r,s) the signature (r, -s (mod N)) is a valid signature of
     * the same message. However, we dislike the ability to modify the bits of a Bitcoin transaction after it's
     * been signed, as that violates various assumed invariants. Thus in future only one of those forms will be
     * considered legal and the other will be banned.
     */
    fun toCanonicalised(): ECDSASignature {
        return if (!isCanonical) {
            // The order of the curve is the number of valid points that exist on that curve. If S is in the upper
            // half of the number of valid points, then bring it back to the lower half. Otherwise, imagine that
            //    N = 10
            //    s = 8, so (-8 % 10 == 2) thus both (r, 8) and (r, 2) are valid solutions.
            //    10 - 8 == 2, giving us always the latter solution, which is canonical.
            ECDSASignature(
                r,
                ECKey.curve.n.subtract(s)
            )
        } else {
            this
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other != null && other is ECDSASignature) {
            return r == other.r && s == other.s
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hashCode(r, s)
    }

}
