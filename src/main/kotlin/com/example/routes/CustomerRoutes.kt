package com.example.routes

import com.example.dao.DatabaseSingleton.dao
import com.example.dao.DatabaseSingleton.usersDigestsDao
import com.example.models.Customer
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.nio.charset.StandardCharsets.UTF_8

fun Route.customerRouting() {
    route("/customer") {
        get {
            val customers = dao?.getAllCustomers()
            if (!customers.isNullOrEmpty()) {
                call.respond(customers)
            } else {
                call.respondText("No customers found", status = HttpStatusCode.OK)
            }
        }
        get("{id?}") {
            val id = call.parameters["id"]?.toInt()
                ?: return@get call.respondText(
                    text = "Missing id query param",
                    status = HttpStatusCode.BadRequest
                )
            val customer = dao?.getCustomer(id)
                ?: return@get call.respondText(
                    text = "No customer with id $id",
                    status = HttpStatusCode.NotFound
                )

            //Authorization: Bearer <token>
            val digest = call.request.headers.get("Authorization")
            val digestExists = if (digest != null) {
                val token = digest.split(" ")[1]
                usersDigestsDao?.validateDigest(token) ?: false
            } else {
                false
            }

            if (digestExists) {
                call.respond(customer)
            } else {
                call.respondText(
                    text = "Auth failed.",
                    status = HttpStatusCode.Unauthorized
                )
            }

            //call.respond(customer)
        }
        post {
            val customer = call.receive<Customer>()
            val addedCustomer = dao?.addNewCustomer(
                firstName = customer.firstName,
                lastName = customer.lastName,
                email = customer.email,
            )

            val digest = addedCustomer?.digest
            //call.respondText("Customer stored correctly, digest=$digest", status = HttpStatusCode.Created)
            call.respond(addedCustomer!!)
        }
        delete("{id?}") {
            val id = call.parameters["id"]?.toInt()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val removeResult = dao?.deleteCustomer(id) ?: false
            if (removeResult) {
                call.respondText("Customer removed correctly", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not Found", status = HttpStatusCode.NotFound)
            }
        }
    }
}