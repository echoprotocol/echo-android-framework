@file:JvmName("Extensions")

package com.pixelplex.echoframework.support

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.pixelplex.echoframework.TIME_DATE_FORMAT
import com.pixelplex.echoframework.model.Account
import com.pixelplex.echoframework.model.Authority
import com.pixelplex.echoframework.model.AuthorityType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Contains all project extensions
 *
 * @author Daria Pechkovskaya
 */

/**
 * Format date to default date format
 *
 * @return formatted date text
 */
fun Date.format(): String = this.format(TIME_DATE_FORMAT)

/**
 * Format date to time format
 *
 * @param dateFormat: new date format
 * @return formatted date text
 */
fun Date.format(dateFormat: String): String {
    val dateFormatter = SimpleDateFormat(dateFormat, Locale.getDefault())
    dateFormatter.timeZone = TimeZone.getTimeZone("GMT")
    return dateFormatter.format(this)
}

/**
 * Converts receiver string to json object with google json parser
 */
fun String.toJsonObject(): JsonObject =
    JsonParser().parse(this).asJsonObject

/**
 * Check account equals by [key] from role [authorityType]
 *
 * @param key Public key from role
 * @param authorityType Role for equals operation
 */
fun Account.isEqualsByKey(key: String, authorityType: AuthorityType): Boolean =
    when (authorityType) {
        AuthorityType.OWNER -> isKeyExist(key, owner)
        AuthorityType.ACTIVE -> isKeyExist(key, active)
        AuthorityType.KEY -> {
            options.memoKey?.address == key
        }
    }

private fun isKeyExist(address: String, authority: Authority): Boolean {
    val foundKey = authority.keyAuthorities.keys.find { pubKey ->
        pubKey.address == address
    }
    return foundKey != null
}
