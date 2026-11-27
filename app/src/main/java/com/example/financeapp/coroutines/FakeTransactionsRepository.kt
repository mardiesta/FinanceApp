package com.example.financeapp.coroutines

import kotlinx.coroutines.delay
import java.io.IOException

private const val LOAD_DELAY_MS = 1_500L

internal data class Transaction(val description: String, val amount: Int)

internal class FakeTransactionsRepository {
    suspend fun load(shouldFail: Boolean): List<Transaction> {
        delay(LOAD_DELAY_MS)
        if (shouldFail) throw IOException("Учебная ошибка загрузки")

        return listOf(Transaction(amount = 250, description = "Кофе"))
    }
}
