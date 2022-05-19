package io.pleo.antaeus.logging

object LoggerFactory {
    fun getLogger() : PleoLogger = DefaultLogger()
}