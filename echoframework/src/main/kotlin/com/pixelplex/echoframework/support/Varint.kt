/*
 * Copyright 2018 Dasha Pechkovskaya
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pixelplex.echoframework.support

import java.io.DataOutput
import java.io.IOException

/**
 * <p>Encodes signed and unsigned values using a common variable-length
 * scheme, found for example in
 * <a href="http://code.google.com/apis/protocolbuffers/docs/encoding.html">
 * Google's Protocol Buffers</a>. It uses fewer bytes to encode smaller values,
 * but will use slightly more bytes to encode large values.</p>
 * <p/>
 * <p>Signed values are further encoded using so-called zig-zag encoding
 * in order to make them "compatible" with variable-length encoding.</p>
 */
object Varint {

    /**
     * Encodes a value using the variable-length encoding from
     * <a href="http://code.google.com/apis/protocolbuffers/docs/encoding.html">
     * Google Protocol Buffers</a>. It uses zig-zag encoding to efficiently
     * encode signed values. If values are known to be nonnegative,
     * @see writeUnsignedVarLong(Long, DataOutput) should be used.
     *
     * @param value value to encode
     * @param out   to write bytes to
     * @throws IOException if [DataOutput] throws [IOException]
     */
    @Throws(IOException::class)
    fun writeSignedVarLong(value: Long, out: DataOutput) {
        // Great trick from http://code.google.com/apis/protocolbuffers/docs/encoding.html#types
        writeUnsignedVarLong(value shl 1 xor (value shr 63), out)
    }

    /**
     * Encodes a value using the variable-length encoding from
     * <a href="http://code.google.com/apis/protocolbuffers/docs/encoding.html">
     * Google Protocol Buffers</a>. Zig-zag is not used, so input must not be negative.
     * If values can be negative, use
     * @see writeSignedVarLong(long, DataOutput) instead. This method treats negative input
     * as like a large unsigned value.
     *
     * @param value value to encode
     * @param out   to write bytes to
     * @throws IOException if [DataOutput] throws [IOException]
     */
    @Throws(IOException::class)
    fun writeUnsignedVarLong(value: Long, out: DataOutput) {
        var newValue = value
        while (newValue and -0x80L != 0L) {
            out.writeByte(newValue.toInt() and 0x7F or 0x80)
            newValue = newValue ushr 7
        }
        out.writeByte(newValue.toInt() and 0x7F)
    }

}
