package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World! and TODO")
        }
        authenticate {
            get("/v1/analysis/ai") {
                call.respond(mapOf("Coming" to "Soon"))
            }
        }
    }
}
