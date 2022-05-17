package io.pleo.antaeus.factories

import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.data.AntaeusDal

object ServiceFactory {

    private var dal : AntaeusDal? = null

    fun setDal(dal : AntaeusDal) {
        this.dal = dal
    }
    fun getCustomerProvider(): CustomerService {
        return CustomerService(dal!!)
    }
}