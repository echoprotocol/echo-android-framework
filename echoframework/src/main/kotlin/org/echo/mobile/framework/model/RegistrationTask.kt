package org.echo.mobile.framework.model

import com.google.common.primitives.UnsignedLong

/**
 * Describes task required to solv for account registration
 */
data class RegistrationTask(
    val blockId: String,
    val randNum: UnsignedLong,
    val difficulty: Int
)