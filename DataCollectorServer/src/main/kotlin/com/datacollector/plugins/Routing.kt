package com.datacollector.plugins

import com.datacollector.auth.SignupBody
import com.datacollector.db.UsersTable
import com.datacollector.utils.isValidEmail
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt


fun Application.configureRouting() {
  routing {
    get("/") {
      call.respondText("Hello World! and TODO")
    }
    route("/v1/signup") {
      install(RequestValidation) {
        validate<SignupBody> { signupBody ->
          if (!signupBody.username.isValidEmail()) {
            ValidationResult.Invalid("Invalid username. Please use an email address.")
          } else {
            ValidationResult.Valid
          }
        }
      }
      post {
        val signupBody = call.receive<SignupBody>()
        val usernameTaken = transaction {
          UsersTable.select(UsersTable.username).where { UsersTable.username eq signupBody.username }.empty().not()
        }
        if (usernameTaken) {
          call.respond(HttpStatusCode.BadRequest, "Sorry, that username is already taken.")
        } else {
          transaction {
            UsersTable.insert {
              it[username] = signupBody.username
              it[hashedPassword] = BCrypt.hashpw(signupBody.password, BCrypt.gensalt())
            }
          }
        }
        call.respond(HttpStatusCode.OK)
      }
    }
    authenticate {
      get("/v1/analysis/ai") {
        call.respond(mapOf("Coming" to "Soon"))
      }
    }
  }
}
