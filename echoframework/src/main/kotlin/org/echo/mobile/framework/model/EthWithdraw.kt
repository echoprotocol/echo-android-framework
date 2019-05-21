package org.echo.mobile.framework.model

/**
 * Describes single ethereum withdraw model
 *
 * @author Dmitriy Bushuev
 */
data class EthWithdraw(
    val id: String,
    val accountId: String,
    val address: String,
    val value: String,
    val isApproved: Boolean,
    val approves: List<String>
)