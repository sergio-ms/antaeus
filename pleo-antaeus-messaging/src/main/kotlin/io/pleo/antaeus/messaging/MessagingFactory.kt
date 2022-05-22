package io.pleo.antaeus.messaging

import io.pleo.antaeus.logging.LoggerFactory

object MessagingFactory {

    var logger = LoggerFactory.getLogger()
    fun getMessageConfiguration() = MessagingConfiguration()
    fun getMessagePublisher() = RabbitMqMessagePublisher(getMessageConfiguration().connInfo, logger)
    fun getMessageConsumer() = RabbitMqMessageConsumer(logger)
}