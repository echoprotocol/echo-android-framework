package org.echo.mobile.framework.core.logger

/**
 * Encapsulates logic of logging library events
 *
 * @author Dmitriy Bushuev
 */
interface Logger {

    /**
     * Logs message with option error payload
     */
    fun log(message: String, error: Throwable? = null)

}
