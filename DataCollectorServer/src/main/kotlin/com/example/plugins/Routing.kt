package com.example.plugins

import com.example.Cities
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureRouting() {
    routing {
        get("/") {
            val woof = transaction {
                Cities.selectAll().first()[Cities.name]
            }
            call.respondText("Hello World! and $woof")
        }
        get("/v1/analysis/ai") {
            call.respond(mapOf("Coming" to "Soon"))
        }
    }
}
