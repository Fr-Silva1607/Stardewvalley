package com.example.stardewvalley.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_granjas")
data class Farm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String
)