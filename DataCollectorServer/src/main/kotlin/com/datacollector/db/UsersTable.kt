package com.datacollector.db

import org.jetbrains.exposed.sql.Table

object UsersTable : Table() {
  val username = varchar("username", 255)
  val hashedPassword = varchar("hashed_password", 60)
  val id = integer("user_id")
}
