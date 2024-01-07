package com.example.dao

import com.example.models.*
import org.ehcache.config.builders.*
import org.ehcache.config.units.*
import org.ehcache.impl.config.persistence.*
import java.io.*

class DAOFacadeCacheImpl(
    private val delegate: DAOFacade,
    storagePath: File,
) : DAOFacade {
    private val cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .with(CacheManagerPersistenceConfiguration(storagePath))
        .withCache(
            "customersCache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                Int::class.javaObjectType,
                Customer::class.java,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                    .heap(1000, EntryUnit.ENTRIES)
                    .offheap(10, MemoryUnit.MB)
                    .disk(100, MemoryUnit.MB, true)
            )
        )
        .build(true)

    private val customersCache = cacheManager.getCache(
        "customersCache",
        Int::class.javaObjectType,
        Customer::class.java
    )

    override suspend fun getAllCustomers(): List<Customer> {
        return delegate.getAllCustomers()
    }

    override suspend fun getCustomer(id: Int): Customer? {
        return customersCache[id]
            ?: delegate.getCustomer(id)
                ?.also { customer -> customersCache.put(id, customer) }
    }

    override suspend fun addNewCustomer(firstName: String, lastName: String, email: String): Customer? {
        return delegate.addNewCustomer(firstName, lastName, email)
            ?.also { customer -> customersCache.put(customer.id, customer) }
    }

    override suspend fun editCustomer(id: Int, firstName: String, lastName: String, email: String): Boolean {
        customersCache.put(
            id,
            Customer(id, firstName, lastName, email)
        )
        return delegate.editCustomer(id, firstName, lastName, email)
    }

    override suspend fun deleteCustomer(id: Int): Boolean {
        customersCache.remove(id)
        return delegate.deleteCustomer(id)
    }
}