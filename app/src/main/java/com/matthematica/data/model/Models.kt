package com.matthematica.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = "",
    val email: String = "",
    val passwordHash: String = "",
    val displayName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "calculation_history")
data class CalculationHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "",
    val expression: String = "",
    val result: String = "",
    val category: String = "", // "algebra", "trigonometry", "chemistry", "calculus", etc.
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

@Serializable
data class CalculationHistoryWithUser(
    val userId: String,
    val items: List<CalculationHistory>
)

