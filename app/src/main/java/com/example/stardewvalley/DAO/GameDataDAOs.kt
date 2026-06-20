package com.example.stardewvalley.DAO

import androidx.room.*
import com.example.stardewvalley.data.*

@Dao
interface CultivoDao {
    @Query("SELECT * FROM tabla_cultivos_plantados WHERE farmId = :farmId")
    fun getCultivosByFarm(farmId: Int): List<CultivoPlantadoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCultivo(cultivo: CultivoPlantadoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCultivos(cultivos: List<CultivoPlantadoEntity>)

    @Query("DELETE FROM tabla_cultivos_plantados WHERE farmId = :farmId")
    fun deleteCultivosByFarm(farmId: Int)
}

@Dao
interface CheckListDao {
    @Query("SELECT * FROM tabla_checklist_progreso WHERE farmId = :farmId")
    fun getProgressByFarm(farmId: Int): List<CheckListItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProgress(item: CheckListItemEntity)

    @Query("DELETE FROM tabla_checklist_progreso WHERE farmId = :farmId")
    fun deleteProgressByFarm(farmId: Int)
}

@Dao
interface JojaDao {
    @Query("SELECT * FROM tabla_joja_progreso WHERE farmId = :farmId")
    fun getProjectsByFarm(farmId: Int): List<JojaProjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProject(project: JojaProjectEntity)

    @Query("DELETE FROM tabla_joja_progreso WHERE farmId = :farmId")
    fun deleteProjectsByFarm(farmId: Int)
}

@Dao
interface GlobalProgressDao {
    @Query("SELECT * FROM tabla_progreso_global WHERE farmId = :farmId")
    fun getGlobalProgress(farmId: Int): GlobalProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveGlobalProgress(progress: GlobalProgressEntity)

    @Query("DELETE FROM tabla_progreso_global WHERE farmId = :farmId")
    fun deleteGlobalProgressByFarm(farmId: Int)
}
