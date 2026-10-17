package com.elnaz.brushbuddy.data.local
import kotlinx.coroutines.flow.Flow
class BrushingSessionRepository(
    private val dao: BrushingSessionDao
) {
    val allSessions: Flow<List<BrushingSession>> =
        dao.getAllSessions()
    suspend fun insertSession(session: BrushingSession) {
        dao.insertSession(session)
    }
    suspend fun deleteSession(session: BrushingSession) {
        dao.deleteSession(session)
    }
}