package com.example.stardewvalley.viewmodel

import androidx.lifecycle.ViewModel
import com.example.stardewvalley.ui.screen.CultivoCargado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CultivoViewModel : ViewModel() {

    // 1. ESTADOS DE DATOS (Nombres unificados para que tu UI los encuentre)

    // Lista que viene del JSON (Datos técnicos)
    private val _listaProcesadaDelJson = MutableStateFlow<List<CultivoCargado>>(emptyList())
    val listaProcesadaDelJson = _listaProcesadaDelJson.asStateFlow()

    // Lista de lo que el usuario ya plantó (Seguimiento)
    private val _cultivosPlantados = MutableStateFlow<List<CultivoPlantado>>(emptyList())
    val cultivosPlantados = _cultivosPlantados.asStateFlow()

    // Estado del Día Actual
    private val _diaActual = MutableStateFlow(1)
    val diaActual = _diaActual.asStateFlow()

    // --- FUNCIONES DE ESTADO ---

    fun setDiaActual(nuevoDia: Int) {
        _diaActual.value = nuevoDia
    }

    fun actualizarSeleccion(nombre: String, nuevaCantidad: Int) {
        _listaProcesadaDelJson.value = _listaProcesadaDelJson.value.map {
            if (it.nombre == nombre) it.copy(cantidad = nuevaCantidad) else it
        }
    }

    fun guardarCultivos(nombre: String, cantidad: Int, diaPlante: Int) {
        if (cantidad > 0) {
            val nuevo = CultivoPlantado(nombre, cantidad, diaPlante)
            _cultivosPlantados.value += nuevo
        }
    }

    // --- LÓGICA DE NEGOCIO (Tu código corregido) ---

    // 1. Para la calculadora de la tienda
    fun calcularRentabilidad(nombre: String, precio: Int, venta: Int, crecimiento: Int, dia: Int, cant: Int): String {
        val diasRestantes = 28 - dia
        if (crecimiento > diasRestantes) {
            return "¡PERDERÁS $${precio * cant}! No alcanza a crecer."
        }
        return "Ganancia estimada: $${(venta - precio) * cant}"
    }

    // 2. Para saber qué dibujo mostrar en el huerto
    fun obtenerImagenFase(nombre: String, diaPlante: Int, diaActual: Int, diasTotales: Int = 12): String {
        val diasPasados = diaActual - diaPlante
        val fase = when {
            diasPasados >= diasTotales -> 6
            diasPasados < 0 -> 1
            else -> {
                val progreso = (diasPasados.toFloat() / diasTotales.toFloat() * 5).toInt() + 1
                progreso.coerceIn(1, 5)
            }
        }
        val nombreLimpio = nombre.lowercase().replace(" ", "_")
        return "fase${fase}_$nombreLimpio"
    }

    // 3. La sugerencia técnica para el portafolio
    fun obtenerMejorOpcion(seleccionados: List<CultivoCargado>, diaActual: Int): String {
        val posibles = seleccionados.filter { it.diasCrecimiento <= (28 - diaActual) }
        val mejor = posibles.maxByOrNull { it.venta - it.precioSemilla }
        return mejor?.let { "Mejor opción: ${it.nombre} (Día $diaActual)" } ?: "No hay tiempo para más cosechas."
    }
}