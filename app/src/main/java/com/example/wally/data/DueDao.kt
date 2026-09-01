package com.example.wally.data
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DueDao {

    @Insert
    suspend fun insertDue(due: Due)

    @Query("DELETE FROM dues WHERE id = :id")
    suspend fun deleteDue(id: Int)

    @Query("SELECT * FROM dues ORDER BY timestamp DESC")
    fun getAllDues(): Flow<List<Due>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM dues WHERE timestamp >= :start AND timestamp < :end")
    fun getTodayTotal(start: Long, end: Long): Flow<Int>

    @Query("SELECT COUNT(DISTINCT name) FROM dues")
    fun getPeopleDue(): Flow<Int>


}
