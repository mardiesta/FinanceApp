package com.example.financeapp.data

import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var transactionDao: TransactionDao
    private lateinit var categoryDao: CategoryDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(AndroidSQLiteDriver())
            .build()
        transactionDao = database.transactionDao()
        categoryDao = database.categoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insert_returnsIdAndRowCanBeReadBack() = runBlocking {
        val id = transactionDao.insert(transaction("Кофе", amountMinor = 25_000L, type = "EXPENSE"))

        val found = transactionDao.findById(id)

        assertEquals("Кофе", found?.description)
    }

    @Test
    fun emptyDatabase_hasZeroBalance() = runBlocking {
        assertEquals(0L, transactionDao.observeBalance().first())
    }

    @Test
    fun income_increasesAndExpense_decreasesBalance() = runBlocking {
        transactionDao.insert(transaction("Зарплата", amountMinor = 50_000L, type = "INCOME"))
        transactionDao.insert(transaction("Кофе", amountMinor = 25_000L, type = "EXPENSE"))

        assertEquals(25_000L, transactionDao.observeBalance().first())
    }

    @Test
    fun delete_updatesObservedRows() = runBlocking {
        val id = transactionDao.insert(transaction("Кофе", amountMinor = 25_000L, type = "EXPENSE"))
        assertEquals(1, transactionDao.observeRows().first().size)

        transactionDao.delete(checkNotNull(transactionDao.findById(id)))

        assertTrue(transactionDao.observeRows().first().isEmpty())
    }

    @Test
    fun leftJoin_keepsTransactionsWithoutCategory() = runBlocking {
        categoryDao.insert(CategoryEntity(name = "Еда", colorArgb = 0xFFE57373L))
        val categoryId = categoryDao.observeAll().first().single().id
        transactionDao.insert(transaction("Без категории", amountMinor = 1_000L, type = "EXPENSE"))
        transactionDao.insert(transaction("Обед", amountMinor = 2_000L, type = "EXPENSE", categoryId = categoryId))

        val rows = transactionDao.observeRows().first()

        assertEquals(2, rows.size)
        assertEquals(1, rows.count { it.categoryName == null })
        assertEquals("Еда", rows.single { it.description == "Обед" }.categoryName)
    }

    @Test
    fun deletingCategory_setsCategoryIdToNull() = runBlocking {
        categoryDao.insert(CategoryEntity(name = "Транспорт", colorArgb = 0xFF64B5F6L))
        val category = categoryDao.observeAll().first().single()
        val id = transactionDao.insert(
            transaction("Метро", amountMinor = 6_000L, type = "EXPENSE", categoryId = category.id),
        )

        categoryDao.delete(category)

        assertNull(transactionDao.findById(id)?.categoryId)
    }

    private fun transaction(description: String, amountMinor: Long, type: String, categoryId: Long? = null) =
        TransactionEntity(
            description = description,
            amountMinor = amountMinor,
            type = type,
            occurredAt = System.currentTimeMillis(),
            categoryId = categoryId,
        )
}
