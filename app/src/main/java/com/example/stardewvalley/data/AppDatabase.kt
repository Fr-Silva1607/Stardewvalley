package com.example.stardewvalley.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.stardewvalley.DAO.CheckListDao
import com.example.stardewvalley.DAO.CultivoDao
import com.example.stardewvalley.DAO.FarmDao
import com.example.stardewvalley.DAO.GlobalProgressDao
import com.example.stardewvalley.DAO.JojaDao

@Database(
    entities = [
        Farm::class,
        CultivoPlantadoEntity::class,
        CheckListItemEntity::class,
        JojaProjectEntity::class,
        GlobalProgressEntity::class
    ],
    version = 6, // Incrementado a 6 para forzar la limpieza y asegurar la integridad por partida
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun farmDao(): FarmDao
    abstract fun cultivoDao(): CultivoDao
    abstract fun checkListDao(): CheckListDao
    abstract fun jojaDao(): JojaDao
    abstract fun globalProgressDao(): GlobalProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stardew_database"
                )
                    .allowMainThreadQueries() // Útil para prototipado
                    .fallbackToDestructiveMigration() // Esto borrará los datos incompatibles y evitará el cierre de la app
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
