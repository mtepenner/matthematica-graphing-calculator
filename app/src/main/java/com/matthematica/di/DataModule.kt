package com.matthematica.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.matthematica.data.database.MatthematicaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val USER_PREFERENCES_NAME = "matthematica_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCES_NAME)

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Singleton
    @Provides
    fun provideMatthematicaDatabase(
        @ApplicationContext context: Context
    ): MatthematicaDatabase {
        return Room.databaseBuilder(
            context,
            MatthematicaDatabase::class.java,
            "matthematica_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideUserDao(database: MatthematicaDatabase) = database.userDao()

    @Singleton
    @Provides
    fun provideCalculationHistoryDao(database: MatthematicaDatabase) =
        database.calculationHistoryDao()

    @Singleton
    @Provides
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}

