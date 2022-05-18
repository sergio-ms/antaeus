package io.pleo.antaeus.messaging

interface MessagePublisher {
    fun connect(connInfo : ConnectionInfo, )
    fun publish(message: String, connInfo : ConnectionInfo)
}