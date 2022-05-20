package io.pleo.antaeus.messaging
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import io.pleo.antaeus.logging.PleoLogger
import java.nio.charset.StandardCharsets

class RabbitMqMessageConsumer(private val logger : PleoLogger) : MessageConsumer {
    override fun subscribe(connectionInfo : QueueConnectionInfo,
                           queue : String,
                           consumerTag : String,
                           deliverAction: (consumerTag : String?, message : String) -> Unit,
                           cancelAction : (consumerTag : String?) -> Unit)
    {
        val factory = ConnectionFactory()
        factory.host = connectionInfo.host
        factory.username = connectionInfo.username
        factory.password = connectionInfo.password
        factory.port = connectionInfo.port
        factory.virtualHost = connectionInfo.virtualHost

        val connection = factory.newConnection()
        val channel = connection.createChannel()
        channel.queueDeclare(queue, true, false, false, null)

        val deliverCallback = DeliverCallback { consumerTag: String?, delivery: Delivery ->
            try {
                val message = String(delivery.body, StandardCharsets.UTF_8)
                deliverAction(consumerTag, message)
            }
            catch (e : Exception)
            {
                logger.logError("Unable to process message. ${e.message}")
            }
        }
        val cancelCallback = CancelCallback { consumerTag: String? ->
            logger.logInfo("Cancelling consumer $consumerTag")
            cancelAction(consumerTag)
        }
        channel.basicConsume(queue, true, consumerTag, deliverCallback, cancelCallback)
    }
}