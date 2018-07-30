@file:JvmName("Extensions")

package com.pixelplex.echoframework.support

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.pixelplex.echoframework.TIME_DATE_FORMAT
import java.text.ParseException
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
 * Parse string to [Date] according to required configuration
 */
@JvmOverloads
fun String.parse(
    format: String = TIME_DATE_FORMAT,
    timeZone: TimeZone = TimeZone.getTimeZone("UTC"),
    locale: Locale = Locale.getDefault(),
    default: Date? = null,
    catch: (e: Exception) -> Unit = {}
): Date? = try {
    val dateFormat = SimpleDateFormat(format, locale).apply {
        this.timeZone = timeZone
    }
    dateFormat.parse(this)
} catch (e: Exception) {
    catch(e)
    default
}

/**
 * Converts receiver string to json object with google json parser
 */
fun String.toJsonObject(): JsonObject? =
    try {
        JsonParser().parse(this).asJsonObject
    } catch (e: Exception) {
        null
    }

