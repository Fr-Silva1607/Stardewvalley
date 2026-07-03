package com.example.stardewvalley.viewmodel

import android.app.Application
import com.example.stardewvalley.R
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.util.Log

sealed class ContenidoPopUp {
    data class Personaje(
        val nombre: String,
        val ama: List<String>,
        val gusta: List<String>,
        val odia: List<String>
    ) : ContenidoPopUp()

    data class Evento(val titulo: String, val descripcion: String) : ContenidoPopUp()

    data class Consejo(val texto: String) : ContenidoPopUp()
}

data class PersonajeJson(
    val nombre: String,
    val cumpleanos: String,
    val gustos: GustosJson
)

data class GustosJson(
    val leEncanta: List<String>,
    val leGusta: List<String>,
    val odia: List<String>
)

data class ConsejoJson(val dia: Int, val consejo: String)

data class CrecimientoJson(
    val nombre: String,
    val fases: List<String>,
    val duracionFases: List<Int>? = null
)

class CalendarioViewModel(application: Application) : AndroidViewModel(application) {
    private val temporadas = listOf("Primavera", "Verano", "Otoño", "Invierno")

    private val _temporadaActualIndex = MutableStateFlow(0)
    val temporadaActualIndex = _temporadaActualIndex.asStateFlow()

    private var consejosData: Map<String, List<ConsejoJson>> = emptyMap()
    private var personajesData: List<PersonajeJson> = emptyList()
    private var crecimientoData: List<CrecimientoJson> = emptyList()

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        try {
            val context = getApplication<Application>().applicationContext
            
            val consejosStream = context.assets.open("consejos.json")
            val consejosString = consejosStream.bufferedReader().use { it.readText() }
            val typeConsejos = object : TypeToken<Map<String, List<ConsejoJson>>>() {}.type
            consejosData = Gson().fromJson(consejosString, typeConsejos)

            val personajesStream = context.assets.open("Personajes.json")
            val personajesString = personajesStream.bufferedReader().use { it.readText() }
            val typePersonajes = object : TypeToken<List<PersonajeJson>>() {}.type
            personajesData = Gson().fromJson(personajesString, typePersonajes)

            val crecimientoStream = context.assets.open("crecimiento.json")
            val crecimientoString = crecimientoStream.bufferedReader().use { it.readText() }
            val typeCrecimiento = object : TypeToken<List<CrecimientoJson>>() {}.type
            crecimientoData = Gson().fromJson(crecimientoString, typeCrecimiento)
            
        } catch (e: Exception) {
            Log.e("CalendarioVM", "Error cargando datos: ${e.message}")
        }
    }

    fun obtenerImagenCumpleanios(dia: Int, temporadaIndex: Int): Int? {
        if (temporadaIndex !in 0..3) return null
        val temporada = temporadas[temporadaIndex]
        val personaje = personajesData.find { it.cumpleanos == "$temporada $dia" }
        val nombreOriginal = personaje?.nombre ?: return null
        
        val nombre = when(nombreOriginal.lowercase()) {
            "emily" -> "emely"
            "vincent" -> "vicent"
            "haley" -> "harley"
            else -> nombreOriginal.lowercase()
        }
        
        val context = getApplication<Application>().applicationContext
        val packageName = context.packageName
        
        val sufijos = listOf("_calendario", "_icono", "")
        for (sufijo in sufijos) {
            val resId = context.resources.getIdentifier("${nombre}$sufijo", "drawable", packageName)
            if (resId != 0) return resId
        }
        
        return R.drawable.placeholder
    }

    fun obtenerContenidoDia(dia: Int, temporadaIndex: Int): ContenidoPopUp {
        val temporada = getNombreTemporada(temporadaIndex)
        
        val personaje = personajesData.find { it.cumpleanos == "$temporada $dia" }
        if (personaje != null) {
            return ContenidoPopUp.Personaje(
                nombre = personaje.nombre,
                ama = personaje.gustos.leEncanta,
                gusta = personaje.gustos.leGusta,
                odia = personaje.gustos.odia
            )
        }

        val listaConsejos = consejosData[temporada]
        val consejoEncontrado = listaConsejos?.find { it.dia == dia }
        
        if (consejoEncontrado != null) {
            return ContenidoPopUp.Consejo(consejoEncontrado.consejo)
        }

        return ContenidoPopUp.Consejo("Día soleado en el valle.")
    }

    fun obtenerImagenFase(nombreCultivo: String, diaActual: Int, diaPlante: Int, diasTotales: Int, creceDeNuevo: Int = 0, replantar: Int = 1): String {
        val diasPasadosDesdePlante = (diaActual - diaPlante).coerceAtLeast(0)
        
        // Lógica de repetición: Si el usuario pidió replantar, las fases se repiten en ciclos
        val mostrarCiclos = replantar > 1
        var diasEnCicloActual = diasPasadosDesdePlante
        
        if (mostrarCiclos) {
            // Si ya pasó el tiempo total de todos los replantes, se queda en la imagen final
            if (diasPasadosDesdePlante >= (diasTotales * replantar)) {
                diasEnCicloActual = diasTotales // Forzar última fase
            } else {
                // Repetir fases según el ciclo actual de replantado
                diasEnCicloActual = diasPasadosDesdePlante % diasTotales
                // Si es el día exacto de cosecha de un ciclo intermedio, mostrar como fase final
                if (diasPasadosDesdePlante > 0 && diasPasadosDesdePlante % diasTotales == 0) {
                    diasEnCicloActual = diasTotales
                }
            }
        } else {
            // Tirada única: No hay módulo, si pasa el tiempo se queda en la fase final (cosecha lista)
            if (diasPasadosDesdePlante > diasTotales) {
                diasEnCicloActual = diasTotales
            }
        }
        
        val nombreNormalizado = normalizar(nombreCultivo)
        val cultivoInfo = crecimientoData.find { normalizar(it.nombre) == nombreNormalizado } ?:
                         crecimientoData.find { nombreNormalizado.contains(normalizar(it.nombre)) }

        if (cultivoInfo != null && cultivoInfo.fases.isNotEmpty()) {
            val fases = cultivoInfo.fases
            val finalImg = fases.last().removeSuffix(".webp").removeSuffix(".png")
            
            // Caso especial: Cultivos que producen continuamente (Café, Judía, etc)
            if (creceDeNuevo > 0 && diasPasadosDesdePlante >= diasTotales) {
                val diasPostCrecimiento = diasPasadosDesdePlante - diasTotales
                val ciclo = diasPostCrecimiento % creceDeNuevo
                if (ciclo == 0) return finalImg
                // Mostrar fase previa a la madurez mientras vuelve a crecer
                return fases[fases.size - 2].removeSuffix(".webp").removeSuffix(".png")
            }

            // Mapeo por duración de fases si el JSON lo tiene
            val duraciones = cultivoInfo.duracionFases
            if (duraciones != null && duraciones.isNotEmpty()) {
                var diasAcumulados = 0
                for (i in duraciones.indices) {
                    diasAcumulados += duraciones[i]
                    if (diasEnCicloActual < diasAcumulados) {
                        return fases[i].removeSuffix(".webp").removeSuffix(".png")
                    }
                }
                return finalImg
            }

            // Mapeo proporcional si no hay duraciones específicas
            val numFasesCrecimiento = fases.size - 1
            val indice = ((diasEnCicloActual.toFloat() / diasTotales.toFloat()) * numFasesCrecimiento).toInt()
            return fases[indice.coerceIn(0, numFasesCrecimiento)].removeSuffix(".webp").removeSuffix(".png")
        }
        
        // Fallback genérico si no hay datos en crecimiento.json
        val faseCalculada = if (diasEnCicloActual >= diasTotales) 6 else ((diasEnCicloActual.toFloat() / diasTotales.toFloat()) * 5).toInt() + 1
        return "fase$faseCalculada${nombreNormalizado}"
    }

    private fun normalizar(texto: String): String =
        texto.lowercase()
            .replace(" ", "")
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("ñ", "n")

    fun siguienteTemporada() {
        if (_temporadaActualIndex.value < 3) _temporadaActualIndex.value++
    }

    fun anteriorTemporada() {
        if (_temporadaActualIndex.value > 0) _temporadaActualIndex.value--
    }

    fun setTemporadaIndex(index: Int) {
        if (index in 0..3) {
            _temporadaActualIndex.value = index
        }
    }

    fun getNombreTemporada(index: Int): String {
        return if (index in 0..3) temporadas[index] else "Primavera"
    }
}
