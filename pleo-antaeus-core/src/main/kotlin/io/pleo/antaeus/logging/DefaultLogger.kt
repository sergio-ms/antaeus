package io.pleo.antaeus.logging

import java.util.logging.Logger

class DefaultLogger : PleoLogger {

        private val logger = Logger.getLogger("DefaultLogger")


    override fun logException(exception: Exception) {
        logger.severe(exception.message)
    }

    override fun logError(error: String) {
        logger.severe(error)
    }

    override fun logWarning(warning: String) {
        logger.warning(warning)
    }

    override fun logInfo(info: String) {
        logger.info(info)
    }
}