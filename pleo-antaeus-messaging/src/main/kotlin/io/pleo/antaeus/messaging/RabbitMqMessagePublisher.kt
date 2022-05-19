package io.pleo.antaeus.messaging

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.ConnectionFactory
import io.pleo.antaeus.logging.PleoLogger
import java.nio.charset.StandardCharsets

class RabbitMqMessagePublisher(private val logger : PleoLogger) : MessagePublisher {
    override fun connect(connInfo: QueueInfo, ) {

    }

    override fun publish(message: String, queueInfo: QueueInfo) {
        try {
            val factory = ConnectionFactory()
            factory.host = queueInfo.connectionInfo.host
            factory.username = queueInfo.connectionInfo.username
            factory.password = queueInfo.connectionInfo.password
            factory.port = queueInfo.connectionInfo.port
            factory.virtualHost = queueInfo.connectionInfo.virtualHost

            val connection = factory.newConnection().use { connection ->

                var channel = connection.createChannel()
                channel.exchangeDeclare(queueInfo.exchange, queueInfo.exchangeType, true)
                channel.queueDeclare(queueInfo.queue, true, false, false, emptyMap())
                channel.queueBind(queueInfo.queue, queueInfo?.exchange, queueInfo.routingKey)

                var props = AMQP.BasicProperties()
                channel.basicPublish(
                    queueInfo?.exchange, queueInfo?.routingKey, props,
                    message.toByteArray(StandardCharsets.UTF_8)
                )
            }
        }
        catch (e: Exception) {
            logger.logError("${this.javaClass.name} Unable to publish message. ${e.message}. $queueInfo.")
        }
    }
}