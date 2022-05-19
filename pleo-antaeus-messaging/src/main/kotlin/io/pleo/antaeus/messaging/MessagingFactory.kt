package io.pleo.antaeus.messaging

import io.pleo.antaeus.logging.LoggerFactory

object MessagingFactory {
    var logger = LoggerFactory.getLogger()
    fun getMessagePublisher() : MessagePublisher = RabbitMqMessagePublisher(logger)
    fun getMessageConsumer() : MessageConsumer = RabbitMqMessageConsumer(logger)
}