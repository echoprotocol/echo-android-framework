package com.pixelplex.echoframework.core.logger.internal

import com.pixelplex.echoframework.core.logger.Logger
import com.pixelplex.echoframework.core.logger.internal.LoggerCoreComponent.LogLevel
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
