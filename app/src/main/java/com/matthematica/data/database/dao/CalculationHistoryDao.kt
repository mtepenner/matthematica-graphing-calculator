package com.matthematica.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.matthematica.data.model.CalculationHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationHistoryDao {
    @Insert
    suspend fun insertHistory(history: CalculationHistory): Long

    @Query("SELECT * FROM calculation_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserHistory(userId: String): Flow<List<CalculationHistory>>

    @Query("SELECT * FROM calculation_history WHERE userId = :userId AND category = :category ORDER BY timestamp DESC")
    fun getUserHistoryByCategory(userId: String, category: String): Flow<List<CalculationHistory>>

    @Query("SELECT * FROM calculation_history WHERE userId = :userId AND isFavorite = 1 ORDER BY timestamp DESC")
    fun getUserFavorites(userId: String): Flow<List<CalculationHistory>>

    @Query("SELECT * FROM calculation_history WHERE userId = :userId AND (expression LIKE :searchQuery OR result LIKE :searchQuery) ORDER BY timestamp DESC")
    fun searchHistory(userId: String, searchQuery: String): Flow<List<CalculationHistory>>

    @Update
    suspend fun updateHistory(history: CalculationHistory)

    @Delete
    suspend fun deleteHistory(history: CalculationHistory)

    @Query("DELETE FROM calculation_history WHERE userId = :userId")
    suspend fun clearUserHistory(userId: String)
}

