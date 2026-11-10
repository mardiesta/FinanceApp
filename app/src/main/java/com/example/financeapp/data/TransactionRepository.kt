package com.example.financeapp.data

import com.example.financeapp.Transaction
import kotlinx.coroutines.flow.Flow

internal interface TransactionRepository {
    val transactions: Flow<List<TransactionRow>>
    val balanceMinor: Flow<Long>
    val categories: Flow<List<CategoryEntity>>

    suspend fun add(transaction: Transaction)

    suspend fun seedDefaultCategoriesIfEmpty()
}

internal class RoomTransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
) : TransactionRepository {
    override val transactions: Flow<List<TransactionRow>> = transactionDao.observeRows()

    override val balanceMinor: Flow<Long> = transactionDao.observeBalance()

    override val categories: Flow<List<CategoryEntity>> = categoryDao.observeAll()

    override suspend fun add(transaction: Transaction) {
        transactionDao.insert(
            TransactionEntity(
                description = transaction.description,
                amountMinor = transaction.amount * MINOR_UNITS,
                type = transaction.type.name,
                occurredAt = System.currentTimeMillis(),
                categoryId = transaction.categoryId,
            ),
        )
    }

    override suspend fun seedDefaultCategoriesIfEmpty() {
        if (categoryDao.count() == 0) {
            DEFAULT_CATEGORIES.forEach { categoryDao.insert(it) }
        }
    }
}

private val DEFAULT_CATEGORIES = listOf(
    CategoryEntity(name = "Еда", colorArgb = 0xFFE57373L),
    CategoryEntity(name = "Транспорт", colorArgb = 0xFF64B5F6L),
    CategoryEntity(name = "Дом", colorArgb = 0xFF81C784L),
)
