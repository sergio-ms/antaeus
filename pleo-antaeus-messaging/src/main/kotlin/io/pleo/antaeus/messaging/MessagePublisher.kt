package io.pleo.antaeus.messaging
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.ConnectionFactory
import java.nio.charset.StandardCharsets

class MessagePublisher() {
    private val connectionName : String = "amqp://guest:guest@localhost:5672/"

    fun publish(exchange : String, queue : String,  message : String) {
        val factory = ConnectionFactory()
        val connection = factory.newConnection(connectionName).use { connection ->
            var routingKey = "default"
            val channel = connection.createChannel()
            channel.exchangeDeclare(exchange, "direct", true)
            channel.queueDeclare(queue, true, false, false, emptyMap())
            channel.queueBind(queue, exchange, routingKey)

            var props = AMQP.BasicProperties()
            channel.basicPublish(
                exchange, "default", props, message.toByteArray(StandardCharsets.UTF_8)
            )
        }
    }
}