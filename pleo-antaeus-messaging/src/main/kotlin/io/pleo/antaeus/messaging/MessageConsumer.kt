package io.pleo.antaeus.messaging

interface MessageConsumer {
    fun subscribe(connectionInfo : ConnectionInfo,
                  queue : String, consumerTag : String,
                  deliverAction: (consumerTag : String?, message : String) -> Unit,
                  cancelAction : (consumerTag : String?) -> Unit)
}