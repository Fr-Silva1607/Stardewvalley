package com.example.stardewvalley.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.stardewvalley.data.Farm

@Dao
interface FarmDao {
    @Query("SELECT * FROM tabla_granjas")
    fun getAllFarms(): List<Farm>

    @Insert
    fun insertFarm(farm: Farm)

    @Update
    fun updateFarm(farm: Farm)

    @Delete
    fun deleteFarm(farm: Farm)
}