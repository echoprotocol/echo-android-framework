/*
 * Copyright 2013 Google Inc.
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

package org.echo.mobile.bitcoinj

import java.io.*
import java.security.Provider
import java.security.SecureRandomSpi
import java.security.Security

/**
 * A SecureRandom implementation that is able to override the standard JVM provided implementation, and which simply
 * serves random numbers by reading /dev/urandom. That is, it delegates to the kernel on UNIX systems and is unusable on
 * other platforms. Attempts to manually set the seed are ignored. There is no difference between seed bytes and
 * non-seed bytes, they are all from the same source.
 *
 * @author Bushuev Dmitriy
 * @author Dasha Pechkovskaya
 */
class LinuxSecureRandom : SecureRandomSpi() {

    private val dis: DataInputStream

    private class LinuxSecureRandomProvider : Provider(
        "LinuxSecureRandom",
        1.0,
        "A Linux specific random number provider that uses /dev/urandom"
    ) {
        init {
            put("SecureRandom.LinuxSecureRandom", LinuxSecureRandom::class.java.name)
        }
    }

    init {
        // DataInputStream is not thread safe, so each random object has its own.
        dis = DataInputStream(uRandom)
    }

    override fun engineSetSeed(bytes: ByteArray) {
        // Ignore.
    }

    override fun engineNextBytes(bytes: ByteArray) =
        try {
            dis.readFully(bytes) // This will block until all the bytes can be read.
        } catch (e: IOException) {
            throw RuntimeException(e) // Fatal error. Do not attempt to recover from this.
        }

    override fun engineGenerateSeed(i: Int): ByteArray {
        val bits = ByteArray(i)
        engineNextBytes(bits)
        return bits
    }

    companion object {
        private const val U_RANDOM_FILE_NAME = "/dev/urandom"

        private val uRandom: FileInputStream

        //    private static final Logger log = LoggerFactory.getLogger(LinuxSecureRandom.class);

        init {
            try {
                val file = File(U_RANDOM_FILE_NAME)
                // This stream is deliberately leaked.
                uRandom = FileInputStream(file)

                if (uRandom.read() == -1)
                    throw RuntimeException("$U_RANDOM_FILE_NAME not readable?")

                // Now override the default SecureRandom implementation with this one.
                Security.insertProviderAt(LinuxSecureRandomProvider(), 1)
            } catch (e: FileNotFoundException) {
                // Should never happen.
                throw RuntimeException(e)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }

        }
    }

}

