package io.pleo.antaeus.logging

interface PleoLogger {
    fun logException(exception : Exception)
    fun logError(error : String)
    fun logWarning(warning : String)
    fun logInfo(info : String)
}