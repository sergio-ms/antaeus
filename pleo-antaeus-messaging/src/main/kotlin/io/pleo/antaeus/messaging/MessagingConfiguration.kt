package io.pleo.antaeus.messaging

// TODO: Configuration should be loaded from a config where values are set at deployment time. For the sake of
//  simplifying the coding challenge the values will be hardcoded
class MessagingConfiguration {

    // Queues
    var pendingInvoicesQueue : String = "PendingInvoicesQueue"
    val customersToInvoiceQueue : String = "CustomersToInvoiceQueue"

    // Exchanges
    val invoicesExchange: String = "InvoicesExchange"
    val customersExchange : String = "CustomersExchange"

    val connectionName : String = "amqp://guest:guest@localhost:5672/"
    val routingKey = "default"
    val exchangeType = "direct"
}