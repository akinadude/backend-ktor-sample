package com.example.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import java.io.Serializable as SerializableMarker//to be used in ehcache

@Serializable
data class Customer(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val digest: String? = null
): SerializableMarker

val customerStorage = mutableListOf<Customer>()

object Customers : Table() {
    val id = integer("id").autoIncrement()
    val firstName = varchar("firstName", 64)
    val lastName = varchar("lastName", 64)
    val email = varchar("email", 64)

    override val primaryKey = PrimaryKey(id)
}

object UsersDigests : Table() {
    val id = integer("id").autoIncrement()
    val digest = varchar("digest", 128)

    override val primaryKey = PrimaryKey(id)
}
