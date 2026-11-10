package com.example.financeapp.data

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection

internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.prepare("ALTER TABLE transactions ADD COLUMN note TEXT NOT NULL DEFAULT ''").use { statement ->
            statement.step()
        }
    }
}
