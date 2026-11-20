package com.example.financeapp.data

import com.example.financeapp.Transaction
import com.example.financeapp.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomTransactionRepositoryTest {
    @Test
    fun add_storesAmountInMinorUnitsAndTypeName() = runBlocking {
        val transactionDao = FakeTransactionDao()
        val repository = RoomTransactionRepository(transactionDao, FakeCategoryDao())

        repository.add(Transaction(250, TransactionType.EXPENSE, "Кофе", 3L))

        val stored = transactionDao.inserted.single()
        assertEquals(25_000L, stored.amountMinor)
        assertEquals("EXPENSE", stored.type)
        assertEquals("Кофе", stored.description)
        assertEquals(3L, stored.categoryId)
    }

    @Test
    fun seedDefaultCategories_runsOnlyWhenEmpty() = runBlocking {
        val categoryDao = FakeCategoryDao()
        val repository = RoomTransactionRepository(FakeTransactionDao(), categoryDao)

        repository.seedDefaultCategoriesIfEmpty()
        val afterFirstSeed = categoryDao.categories.size
        repository.seedDefaultCategoriesIfEmpty()

        assertEquals(3, afterFirstSeed)
        assertEquals(afterFirstSeed, categoryDao.categories.size)
    }
}

private class FakeTransactionDao : TransactionDao {
    val inserted = mutableListOf<TransactionEntity>()

    override suspend fun insert(item: TransactionEntity): Long {
        inserted += item
        return inserted.size.toLong()
    }

    override suspend fun findById(id: Long): TransactionEntity? = inserted.getOrNull(id.toInt() - 1)

    override suspend fun update(item: TransactionEntity) {
        inserted.replaceAll { if (it.id == item.id) item else it }
    }

    override suspend fun delete(item: TransactionEntity) {
        inserted.remove(item)
    }

    override fun observeAll(): Flow<List<TransactionEntity>> = flowOf(inserted.toList())

    override fun observeByType(type: String): Flow<List<TransactionEntity>> =
        flowOf(inserted.filter { it.type == type })

    override fun observeBalance(): Flow<Long> = flowOf(0L)

    override fun observeRows(): Flow<List<TransactionRow>> = flowOf(emptyList())
}

private class FakeCategoryDao : CategoryDao {
    val categories = mutableListOf<CategoryEntity>()

    override suspend fun insert(item: CategoryEntity): Long {
        categories += item
        return categories.size.toLong()
    }

    override suspend fun delete(item: CategoryEntity) {
        categories.remove(item)
    }

    override fun observeAll(): Flow<List<CategoryEntity>> = flowOf(categories.toList())

    override suspend fun count(): Int = categories.size
}
