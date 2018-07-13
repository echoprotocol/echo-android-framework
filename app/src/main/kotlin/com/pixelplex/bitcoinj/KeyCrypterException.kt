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

/**
 * Exception to provide the following:
 *
 * Provision of encryption / decryption exception
 *
 * This base exception acts as a general failure mode not attributable to a specific cause (other than
 * that reported in the exception message). Since this is in English, it may not be worth reporting directly
 * to the user other than as part of a "general failure to parse" response.
 *
 * @author Dasha Pechkovskaya
 * @author Bushuev Dmitriy
 */
class KeyCrypterException : RuntimeException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}
