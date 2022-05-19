package io.pleo.antaeus.messaging

interface MessagePublisher {
    fun connect(connInfo : QueueInfo, )
    fun publish(message: String, connInfo : QueueInfo)
}