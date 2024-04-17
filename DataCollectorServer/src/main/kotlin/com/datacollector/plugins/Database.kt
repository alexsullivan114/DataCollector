package com.datacollector.plugins

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

fun configureDatabase() {
  val dbUrl = System.getenv("DB_URL")
  val dbUser = System.getenv("DB_USER") ?: ""
  val dbPassword = System.getenv("DB_PASSWORD") ?: ""

  Database.connect(url = dbUrl, user = dbUser, password = dbPassword)

  val flyway = Flyway.configure().dataSource(dbUrl, dbUser, dbPassword).load()
  flyway.migrate()
}
