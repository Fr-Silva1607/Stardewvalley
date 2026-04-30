package com.example.stardewvalley.util

object StardewCalculator {
    const val DIAS_POR_ESTACION = 28
    const val ESTACIONES_POR_ANIO = 4

    // Un año tiene 112 días
    private const val TOTAL_DIAS_ANIO = DIAS_POR_ESTACION * ESTACIONES_POR_ANIO

    fun calcularTiempoRealAnio(segundosPorDia: Int): Double {
        val totalSegundos = TOTAL_DIAS_ANIO * segundosPorDia
        return totalSegundos / 3600.0 // Lo pasamos a horas
    }
}