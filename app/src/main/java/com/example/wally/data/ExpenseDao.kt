package com.example.wally.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: Int)

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE timestamp >= :start AND timestamp < :end")
    fun getTodayTotal(start: Long, end: Long): Flow<Int>

    @Query("SELECT * FROM expenses WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp DESC")
    fun getTodayExpenses(start: Long, end: Long): Flow<List<Expense>>


}
