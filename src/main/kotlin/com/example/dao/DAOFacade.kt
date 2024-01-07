package com.example.dao

import com.example.models.Customer

interface DAOFacade {
    suspend fun getAllCustomers(): List<Customer>
    suspend fun getCustomer(id: Int): Customer?
    suspend fun addNewCustomer(
        firstName: String,
        lastName: String,
        email: String
    ): Customer?

    suspend fun editCustomer(
        id: Int,
        firstName: String,
        lastName: String,
        email: String
    ): Boolean

    suspend fun deleteCustomer(id: Int): Boolean
}