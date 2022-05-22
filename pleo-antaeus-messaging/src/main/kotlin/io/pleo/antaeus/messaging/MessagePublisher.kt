package io.pleo.antaeus.messaging

interface MessagePublisher {
    fun publish(message: String, connInfo : QueueInfo)
}