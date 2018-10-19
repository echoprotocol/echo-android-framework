package org.echo.mobile.framework.model.contract.input

/**
 * Describes value [value] and it's type [type] of contract method parameter
 *
 * @author Dmitriy Bushuev
 */
data class InputValue(val type: InputValueType, val value: String)
