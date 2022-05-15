package io.pleo.antaeus.messaging
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import java.nio.charset.StandardCharsets

class MessageConsumer {
    private val connectionName : String = "amqp://guest:guest@localhost:5672/"
    fun subscribe(queue : String, consumerTag : String,
                  deliverAction: (consumerTag : String?, message : String) -> Unit,
                  cancelAction : (consumerTag : String?) -> Unit) {
        val factory = ConnectionFactory()
        val connection = factory.newConnection(connectionName)
        val channel = connection.createChannel()
        channel.queueDeclare(queue, true, false, false, null)

        val deliverCallback = DeliverCallback { consumerTag: String?, delivery: Delivery ->
            val message = String(delivery.body, StandardCharsets.UTF_8)
            deliverAction(consumerTag, message)
        }
        val cancelCallback = CancelCallback { consumerTag: String? ->
            cancelAction(consumerTag)
        }
        channel.basicConsume(queue, true, consumerTag, deliverCallback, cancelCallback)
    }
}