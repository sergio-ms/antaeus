package io.pleo.antaeus.messaging

class ConnectionInfo() {

    var connectionName: String = ""
            get() = field
            set(value) { field = value }

    var exchange: String = ""
        get() = field
        set(value) { field = value }

    var  exchangeType: String = ""

    var queue: String = ""
        get() = field
        set(value) { field = value }

    var type: String = ""
        get() = field
        set(value) { field = value }

    var routingKey: String = ""
        get() = field
        set(value) { field = value }

}