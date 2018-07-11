@file:JvmName("Assertion")

package com.pixelplex.echolib.support

/**
 * Contains all base precondition functions
 *
 * @author Dmitriy Bushuev
 */

/**
 * Throws [IllegalArgumentException] if input value is null
 *
 * @param target Value for null checking
 * @param errorMessage Exception message
 */
fun checkNotNull(target: Any?, errorMessage: String) {
    if (target == null) {
        throw IllegalArgumentException(errorMessage)
    }
}

/**
 * Throws [IllegalArgumentException] if input value is not true
 *
 * @param target Value for true assertion
 * @param errorMessage Exception message
 */
fun checkTrue(target: Boolean, errorMessage: String) {
    if (!target) {
        throw IllegalArgumentException(errorMessage)
    }
}

/**
 * Throws [IllegalArgumentException] if input value is not false
 *
 * @param target Value for false assertion
 * @param errorMessage Exception message
 */
fun checkFalse(target: Boolean, errorMessage: String) {
    if (target) {
        throw IllegalArgumentException(errorMessage)
    }
}


