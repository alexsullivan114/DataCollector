package com.datacollector.db

import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.DatabaseMetaData
import java.sql.ResultSet

fun printDatabase() {
  transaction {
    addLogger(StdOutSqlLogger)
    val conn = TransactionManager.current().connection as JdbcConnectionImpl
    val statement = conn.connection.createStatement()
    val sql = """
                            SELECT 
                                t.TABLE_NAME,
                                c.COLUMN_NAME,
                                c.COLUMN_DEFAULT
                            FROM INFORMATION_SCHEMA.TABLES t
                            JOIN INFORMATION_SCHEMA.COLUMNS c ON t.TABLE_NAME = c.TABLE_NAME
                            WHERE t.TABLE_SCHEMA = 'PUBLIC'
                            ORDER BY t.TABLE_NAME, c.ORDINAL_POSITION
                        """.trimIndent()
    statement.execute(sql)
    val tables = statement.resultSet
    val meta: DatabaseMetaData = conn.connection.metaData
    while (tables.next()) {
      val tableName = tables.getString("TABLE_NAME")
      println("Table: $tableName")

      val columns: ResultSet = meta.getColumns(null, "PUBLIC", tableName, "%")
      while (columns.next()) {
        val columnName = columns.getString("COLUMN_NAME")
        val columnType = columns.getString("TYPE_NAME")
        println(" - Column: $columnName Type: $columnType")
      }
    }
  }
}
