package com.example.stardewvalley.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.stardewvalley.DAO.FarmDao

@Database(entities = [Farm::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun farmDao(): FarmDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Si la instancia ya existe, la devuelve; si no, la crea
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stardew_database" // El nombre del archivo en el celular
                )
                    .allowMainThreadQueries() // Permite ejecutar en el hilo principal (útil para tu portafolio)
                    .fallbackToDestructiveMigration() // Si cambias la tabla, borra y recrea (evita errores en desarrollo)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}