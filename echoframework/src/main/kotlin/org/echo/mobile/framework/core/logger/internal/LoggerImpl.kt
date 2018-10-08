package org.echo.mobile.framework.core.logger.internal

import org.echo.mobile.framework.core.logger.Logger
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent.LogLevel
import java.util.logging.Level

/**
 * Implementation of [Logger]
 *
 * Uses standard java logger
 *
 * @author Dmitriy Bushuev
 */
class LoggerImpl(name: String) : Logger {

    private val logger = java.util.logging.Logger.getLogger(name)

    override fun log(message: String, error: Throwable?) {
        if (LoggerCoreComponent.logLevel == LogLevel.INFO) {
            logger.log(Level.INFO, message, error)
        }
    }

}
