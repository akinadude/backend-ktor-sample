package com.example.dao

import com.example.models.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*
import org.jetbrains.exposed.sql.transactions.experimental.*
import java.io.File
import javax.sql.DataSource

object DatabaseSingleton {

    var dao: DAOFacade? = null
    var usersDigestsDao: DAOUsersAuthDigestsFacade? = null

    fun init(config: ApplicationConfig) {
        val driverClassName = "org.h2.Driver"
        val jdbcURL = "jdbc:h2:file:./build/db"
        /*val driverClassName = config.property("storage.driverClassName").getString()
        val jdbcURL = config.property("storage.jdbcURL").getString() +
                (config.propertyOrNull("storage.dbFilePath")?.getString()?.let { path ->
                    File(path).canonicalFile.absolutePath
                } ?: "")*/

        val dataSource = createHikariDataSource(url = jdbcURL, driver = driverClassName)
        val database = Database.connect(dataSource)

        transaction(database) {
            SchemaUtils.create(Customers)
            SchemaUtils.create(UsersDigests)
        }

        /*dao = DAOFacadeImpl().apply {
            runBlocking {
                if (getAllCustomers().isEmpty()) {
                    addNewCustomer(
                        firstName = "first name example",
                        lastName = "last name example",
                        email = "email@example.com"
                    )
                }
            }
        }*/
        dao = DAOFacadeCacheImpl(
            DAOFacadeImpl(),
            File("build/ehcache")
        ).apply {
            runBlocking {
                if (getAllCustomers().isEmpty()) {
                    addNewCustomer(
                        firstName = "first name example",
                        lastName = "last name example",
                        email = "email@example.com"
                    )
                }
            }
        }
        usersDigestsDao = DAOUsersAuthDigestFacadeImpl()
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T {
        return newSuspendedTransaction(Dispatchers.IO) { block() }
    }

    private fun createHikariDataSource(
        url: String,
        driver: String
    ): DataSource {
        return HikariDataSource(HikariConfig().apply {
            driverClassName = driver
            jdbcUrl = url
            maximumPoolSize = 4
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        })
    }
}