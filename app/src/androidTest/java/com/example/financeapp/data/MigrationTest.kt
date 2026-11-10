package com.example.financeapp.data

import androidx.room3.Room
import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val databaseFile = instrumentation.targetContext.getDatabasePath(TEST_DB)

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = instrumentation,
        file = databaseFile,
        driver = AndroidSQLiteDriver(),
        databaseClass = AppDatabase::class,
        databaseFactory = {
            Room.databaseBuilder<AppDatabase>(instrumentation.targetContext, databaseFile.path)
                .setDriver(AndroidSQLiteDriver())
                .addMigrations(MIGRATION_1_2)
                .build()
        },
    )

    @Test
    fun migrate1To2_keepsRowsAndAddsEmptyNote() = runBlocking {
        helper.createDatabase(1).apply {
            prepare(
                "INSERT INTO transactions (description, amount_minor, type, occurred_at) " +
                    "VALUES ('Кофе', 25000, 'EXPENSE', 0)",
            ).use { it.step() }
            close()
        }

        val connection = helper.runMigrationsAndValidate(2, listOf(MIGRATION_1_2))
        connection.prepare("SELECT description, note FROM transactions").use { statement ->
            assertTrue(statement.step())
            assertEquals("Кофе", statement.getText(0))
            assertEquals("", statement.getText(1))
        }
        connection.close()
    }

    private companion object {
        const val TEST_DB = "migration-test.db"
    }
}
