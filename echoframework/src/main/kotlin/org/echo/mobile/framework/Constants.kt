@file:JvmName("Constants")

package org.echo.mobile.framework

/**
 * Contains all project constants
 *
 * @author Daria Pechkovskaya
 */

/**
 * Time format used across the platform
 */
const val TIME_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

/**
 * Socket url for echo blockchain
 */
const val ECHO_URL = "wss://echo-devnet-node.pixelplex.io/"

/**
 * Illegal id for all blockchain apis, call id
 */
const val ILLEGAL_ID = -1

/**
 * Id of ECHO asset
 */
const val ECHO_ASSET_ID = "1.3.0"

/**
 * Default amount of gas limit for contract call
 */
const val DEFAULT_GAS_LIMIT = 1000000L

/**
 * Default price of gas unit for contract call
 */
const val DEFAULT_GAS_PRICE = 0L
