@file:JvmName("Extensions")

package com.pixelplex.echolib.support

import com.pixelplex.echolib.TIME_DATE_FORMAT
import com.pixelplex.echolib.model.Account
import com.pixelplex.echolib.model.AuthorityType
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
