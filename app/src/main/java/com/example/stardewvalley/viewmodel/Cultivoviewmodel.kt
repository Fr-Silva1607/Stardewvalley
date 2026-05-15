package com.example.stardewvalley.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


// ─── Data classes ────────────────────────────────────────────

data class CultivoCargado(
    val id              : Int    = 0,
    val nombre          : String = "",
    val imagen          : String = "",
    val precioSemilla   : Int    = 0,
    val diasCrecimiento : Int    = 1,
    val cosechaMaximaPorEstacion: Int = 1,
    @SerializedName("Venta")          // El JSON usa "Venta" con mayúscula
    val venta           : Int    = 0,
    val beneficioPorDia : Double = 0.0,
    val estacion        : String = "",
    val año             : Int    = 1,
    var cantidad        : Int    = 0   // Controlado desde la UI
)

data class CultivoPlantado(
    val nombre: String,
    val cantidad: Int,
    val diaPlante: Int,
    val diasCrecimiento: Int = 12
)  // Esto es válido


// ─── ViewModel ───────────────────────────────────────────────

class CultivoViewModel(application: Application) : AndroidViewModel(application) {

    private val _listaProcesadaDelJson = MutableStateFlow<List<CultivoCargado>>(emptyList())
    val listaProcesadaDelJson = _listaProcesadaDelJson.asStateFlow()

    private val _cultivosPlantados = MutableStateFlow<List<CultivoPlantado>>(emptyList())
    val cultivosPlantados = _cultivosPlantados.asStateFlow()

    private val _diaActual = MutableStateFlow(1)
    val diaActual = _diaActual.asStateFlow()

    init {
        cargarCultivosDesdeJson()
    }

    // ── Carga del JSON ──────────────────────────────────────
    private fun cargarCultivosDesdeJson() {
        try {
            val context     = getApplication<Application>().applicationContext
            val inputStream = context.assets.open("Cultivo.json")
            val listType    = object : TypeToken<List<CultivoCargado>>() {}.type
            val lista: List<CultivoCargado> = Gson().fromJson(inputStream.bufferedReader(), listType)
            _listaProcesadaDelJson.value = lista
            Log.d("STARDW", "JSON cargado: ${lista.size} cultivos")
        } catch (e: Exception) {
            Log.e("STARDW_ERROR", "Error cargando JSON: ${e.message}")
        }
    }

    // ── Día actual ──────────────────────────────────────────
    fun setDiaActual(nuevoDia: Int) {
        _diaActual.value = nuevoDia
    }

    // ── Selección de cantidad desde la tabla ────────────────
    fun actualizarSeleccion(nombre: String, nuevaCantidad: Int) {
        _listaProcesadaDelJson.value = _listaProcesadaDelJson.value.map {
            if (it.nombre == nombre) it.copy(cantidad = nuevaCantidad) else it
        }
    }

    // ── Plantar selección ───────────────────────────────────
    fun guardarCultivosMasivo() {
        val nuevos = _listaProcesadaDelJson.value
            .filter { it.cantidad > 0 }
            .map {
                CultivoPlantado(
                    nombre          = it.nombre,
                    cantidad        = it.cantidad,
                    diaPlante       = _diaActual.value,
                    diasCrecimiento = it.diasCrecimiento
                )
            }
        _cultivosPlantados.value = _cultivosPlantados.value + nuevos

        // Resetea las cantidades en la tabla después de plantar
        _listaProcesadaDelJson.value = _listaProcesadaDelJson.value.map { it.copy(cantidad = 0) }
    }

    // ── Rentabilidad ────────────────────────────────────────
    fun calcularRentabilidad(
        precio     : Int,
        venta      : Int,
        crecimiento: Int,
        dia        : Int,
        cant       : Int
    ): String {
        val diasRestantes = 28 - dia
        if (crecimiento > diasRestantes) {
            return "¡PERDERÁS $${precio * cant}! No alcanza a crecer (${crecimiento}d > ${diasRestantes}d restantes)."
        }
        val ganancia = (venta - precio) * cant
        return if (ganancia >= 0) "Ganancia estimada: $$ganancia" else "Pérdida estimada: $$ganancia"
    }

    // ── Imagen de fase ──────────────────────────────────────
    //  Devuelve la clave usada en drawableMap, p.ej.: "fase3_coliflor"
    fun obtenerImagenFase(
        nombre      : String,
        diaPlante   : Int,
        diaActual   : Int,
        diasTotales : Int = 12
    ): String {
        val dias = diasTotales.takeIf { it > 0 } ?: 12
        val diasPasados = diaActual - diaPlante
        val fase = when {
            diasPasados >= dias -> 6
            diasPasados < 0     -> 1
            else -> ((diasPasados.toFloat() / dias.toFloat()) * 5).toInt() + 1
        }
        val clave = nombre.lowercase().replace(" ", "_")
        return "fase${fase}_$clave"
    }

    // ── Mejor opción ────────────────────────────────────────
    fun obtenerMejorOpcion(seleccionados: List<CultivoCargado>, diaActual: Int): String {
        val viables = seleccionados.filter {
            (28 - diaActual) >= it.diasCrecimiento
        }
        if (viables.isEmpty()) return "No hay cultivos viables para plantar en el día $diaActual."

        val mejor = viables.maxByOrNull { it.venta - it.precioSemilla }
        return mejor?.let {
            "¡Planta ${it.nombre} para máxima ganancia! ($${ it.venta - it.precioSemilla } por unidad)"
        } ?: "Sin datos."
    }
}