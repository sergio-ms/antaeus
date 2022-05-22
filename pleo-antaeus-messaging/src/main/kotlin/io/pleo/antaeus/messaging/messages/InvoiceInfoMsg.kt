package io.pleo.antaeus.messaging.messages

import kotlinx.serialization.Serializable


@Serializable
data class InvoiceInfoMsg (val invoiceIds : List<Int>)
{

    companion object{
        const val maxMsgSize : Int = 10
    }
}
