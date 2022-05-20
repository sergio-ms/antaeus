package io.pleo.antaeus.messaging

class QueueInfo(
    var exchange: String,
    var  exchangeType: String,
    var queue: String,
    var type: String,
    var routingKey: String,
)
