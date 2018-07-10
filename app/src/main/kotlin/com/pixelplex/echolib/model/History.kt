package com.pixelplex.echolib.model

/**
 * Represents account history
 *
 * <p>
 *     Contains required information about user operations
 * </p>
 *
 * Created by Dasha on 09.07.2018
 */
class History(
    val id: String,
    val operationType: OperationType,
    val operation: BaseOperation
)
