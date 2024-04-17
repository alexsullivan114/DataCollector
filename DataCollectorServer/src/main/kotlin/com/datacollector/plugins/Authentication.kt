package com.datacollector.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*

fun Application.configureAuthentication() {
  install(Authentication) {
    basic {
      validate {
        null
      }
    }
  }
}
