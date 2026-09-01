package com.example.wally.data
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dues")
data class Due(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val amount: Int,
    val tag: String,
    val timestamp: Long = System.currentTimeMillis()
)
