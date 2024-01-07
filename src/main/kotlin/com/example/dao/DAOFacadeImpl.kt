package com.example.dao

import com.example.dao.DatabaseSingleton.dbQuery
import com.example.models.Customer
import com.example.models.Customers
import com.example.models.UsersDigests
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest

class DAOFacadeImpl : DAOFacade {

    override suspend fun getAllCustomers(): List<Customer> {
        return dbQuery {
            Customers.selectAll().map(::resultRowToCustomer)
        }
    }

    override suspend fun getCustomer(id: Int): Customer? {
        return dbQuery {
            Customers
                .select { Customers.id eq id }
                .map(::resultRowToCustomer)
                .singleOrNull()
        }
    }

    override suspend fun addNewCustomer(
        firstName: String,
        lastName: String,
        email: String
    ): Customer? {
        return dbQuery {
            val insertStatement = Customers.insert {
                it[Customers.firstName] = firstName
                it[Customers.lastName] = lastName
                it[Customers.email] = email
            }

            val createdCustomer = insertStatement.resultedValues
                ?.singleOrNull()?.let(::resultRowToCustomer)

            if (createdCustomer != null) {
                val s = "$firstName:$lastName$email"
                val digest = getCustomerMd5Digest(s).toString()
                UsersDigests.insert {
                    it[UsersDigests.id] = createdCustomer.id
                    it[UsersDigests.digest] = digest
                }

                return@dbQuery createdCustomer.copy(digest = digest)
            }

            createdCustomer
        }
    }

    override suspend fun editCustomer(
        id: Int,
        firstName: String,
        lastName: String,
        email: String
    ): Boolean {
        return dbQuery {
            Customers.update({ Customers.id eq id }) {
                it[Customers.firstName] = firstName
                it[Customers.lastName] = lastName
                it[Customers.email] = email
            } > 0
        }
    }

    override suspend fun deleteCustomer(id: Int): Boolean {
        return dbQuery {
            Customers.deleteWhere { Customers.id eq id } > 0
        }
    }

    private fun resultRowToCustomer(row: ResultRow, digest: String? = null): Customer {
        return Customer(
            id = row[Customers.id],
            firstName = row[Customers.firstName],
            lastName = row[Customers.lastName],
            email = row[Customers.email],
            digest = digest,
        )
    }

    private fun getCustomerMd5Digest(s: String): ByteArray {
        return MessageDigest.getInstance("MD5")
            .digest(s.toByteArray(UTF_8))
    }
}