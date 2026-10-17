package com.elnaz.brushbuddy.data.local
//A DAO is an interface that defines database operations.
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
//Insert, retrieve and delete sessions
@Dao
interface BrushingSessionDao {
    @Insert
    suspend fun insertSession(session: BrushingSession)

    @Query("SELECT * FROM brushing_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<BrushingSession>>

    @Delete
    suspend fun deleteSession(session:BrushingSession)
}