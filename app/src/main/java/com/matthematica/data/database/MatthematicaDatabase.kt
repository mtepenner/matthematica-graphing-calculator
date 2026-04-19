package com.matthematica.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.matthematica.data.database.dao.CalculationHistoryDao
import com.matthematica.data.database.dao.UserDao
import com.matthematica.data.model.CalculationHistory
import com.matthematica.data.model.User
import java.time.Instant

@Database(
    entities = [User::class, CalculationHistory::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MatthematicaDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun calculationHistoryDao(): CalculationHistoryDao
}

class Converters {
    fun fromTimestamp(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }
    fun dateToTimestamp(date: Instant?): Long? = date?.toEpochMilli()
}

