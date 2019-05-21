package org.echo.mobile.framework.model

/**
 * Describes single ethereum deposit model
 *
 * @author Dmitriy Bushuev
 */
data class EthDeposit(
    val id: String,
    val accountId: String,
    val value: String,
    val isApproved: Boolean,
    val approves: List<String>
)