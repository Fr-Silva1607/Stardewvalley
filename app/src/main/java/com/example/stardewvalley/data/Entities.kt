package com.example.stardewvalley.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tabla_cultivos_plantados",
    foreignKeys = [
        ForeignKey(
            entity = Farm::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CultivoPlantadoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val farmId: Int,
    val nombre: String,
    val cantidad: Int,
    val diaPlante: Int,
    val diasCrecimiento: Int,
    val creceDeNuevo: Int,
    val multiplicadorCosecha: Int,
    val replantar: Int
)

@Entity(
    tableName = "tabla_checklist_progreso",
    primaryKeys = ["farmId", "nombreSala", "nombreLote", "itemNombre"],
    foreignKeys = [
        ForeignKey(
            entity = Farm::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CheckListItemEntity(
    val farmId: Int,
    val nombreSala: String,
    val nombreLote: String,
    val itemNombre: String,
    val completado: Boolean
)

@Entity(
    tableName = "tabla_joja_progreso",
    primaryKeys = ["farmId", "projectKey"],
    foreignKeys = [
        ForeignKey(
            entity = Farm::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class JojaProjectEntity(
    val farmId: Int,
    val projectKey: String,
    val aportado: Int
)

@Entity(
    tableName = "tabla_progreso_global",
    foreignKeys = [
        ForeignKey(
            entity = Farm::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GlobalProgressEntity(
    @PrimaryKey val farmId: Int,
    val diaActual: Int,
    val dineroActual: Int,
    val rutaElegida: String? = null // "Centro Civico" o "Joja"
)
