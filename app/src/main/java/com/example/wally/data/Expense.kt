package com.example.wally.data
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val amount: Int,
    val tag: String,
    val timestamp: Long = System.currentTimeMillis()
)
