package io.pleo.antaeus.messaging

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import io.pleo.antaeus.logging.PleoLogger
import java.nio.charset.StandardCharsets

class RabbitMqMessagePublisher(connectionInfo : QueueConnectionInfo, private val logger : PleoLogger) : MessagePublisher {
    private val factory : ConnectionFactory = ConnectionFactory()

    init {
        factory.host = connectionInfo.host
        factory.username = connectionInfo.username
        factory.password = connectionInfo.password
        factory.port = connectionInfo.port
        factory.virtualHost = connectionInfo.virtualHost
    }

    override fun publish(message: String, queueInfo: QueueInfo) {
        try {
            factory.newConnection().use { connection ->
                var channel = connection?.createChannel()
                channel?.exchangeDeclare(queueInfo.exchange, queueInfo.exchangeType, true)
                channel?.queueDeclare(queueInfo.queue, true, false, false, emptyMap())
                channel?.queueBind(queueInfo.queue, queueInfo?.exchange, queueInfo.routingKey)

                var props = AMQP.BasicProperties()
                channel?.basicPublish(
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