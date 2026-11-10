package com.example.financeapp.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface TransactionDao {
    @Insert
    suspend fun insert(item: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): TransactionEntity?

    @Update
    suspend fun update(item: TransactionEntity)

    @Delete
    suspend fun delete(item: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY occurred_at DESC, id DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY occurred_at DESC, id DESC")
    fun observeByType(type: String): Flow<List<TransactionEntity>>

    @Query(
        "SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount_minor ELSE -amount_minor END), 0) " +
            "FROM transactions",
    )
    fun observeBalance(): Flow<Long>

    @Query(
        """
        SELECT t.id AS id, t.description AS description, t.amount_minor AS amountMinor,
               t.type AS type, c.name AS categoryName
        FROM transactions t
        LEFT JOIN categories c ON c.id = t.category_id
        ORDER BY t.occurred_at DESC, t.id DESC
        """,
    )
    fun observeRows(): Flow<List<TransactionRow>>
}
