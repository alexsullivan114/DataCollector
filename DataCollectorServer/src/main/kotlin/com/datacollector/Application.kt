package com.datacollector

import com.datacollector.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureDatabase()
    configureAuthentication()
    configureSerialization()
    configureRouting()
    install(StatusPages) {
        exception<ContentTransformationException> { call, status ->
            call.respond(HttpStatusCode.BadRequest, "Could not process request body: ${status.message}")
        }
        exception<RequestValidationException> { call, status ->
            call.respond(HttpStatusCode.BadRequest, "${status.reasons.firstOrNull()}")
        }
    }
}
