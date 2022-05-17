package io.pleo.antaeus.messaging

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import java.nio.charset.StandardCharsets

class RabbitMqMessagePublisher() : MessagePublisher {
    private val connectionName: String = "amqp://guest:guest@localhost:5672/"

    //private var channel: Channel? = null
    private var connInfo : ConnectionInfo? = null

    override fun connect(connInfo : ConnectionInfo, ) {
        this.connInfo = connInfo
        val factory = ConnectionFactory()
        val connection = factory.newConnection(connectionName).use { connection ->

            var channel = connection.createChannel()
            channel.exchangeDeclare(connInfo.exchange, connInfo.exchangeType, true)
            channel.queueDeclare(connInfo.queue, true, false, false, emptyMap())
            channel.queueBind(connInfo.queue, connInfo.exchange, connInfo.routingKey)
        }
    }

    override fun publish(message: String) {
        val factory = ConnectionFactory()
        val connection = factory.newConnection(connectionName).use { connection ->

            var channel = connection.createChannel()
            channel.exchangeDeclare(connInfo?.exchange, connInfo?.exchangeType, true)
            channel.queueDeclare(connInfo?.queue, true, false, false, emptyMap())
            channel.queueBind(connInfo?.queue, connInfo?.exchange, connInfo?.routingKey)

            // TODO: Check if channel is null and throw exception
            var props = AMQP.BasicProperties()
            channel.basicPublish(connInfo?.exchange, connInfo?.routingKey, props, message.toByteArray(StandardCharsets.UTF_8)
            )
        }



    }
}
