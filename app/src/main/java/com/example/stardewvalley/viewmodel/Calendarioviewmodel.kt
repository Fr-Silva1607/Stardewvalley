package com.example.stardewvalley.viewmodel

import com.example.stardewvalley.R
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

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

class CalendarioViewModel : ViewModel() {
    private val temporadas = listOf("Primavera", "Verano", "Otoño", "Invierno")

    // Estado del índice de la temporada
    private val _temporadaActualIndex = MutableStateFlow(0)
    val temporadaActualIndex = _temporadaActualIndex.asStateFlow()

    // --- SECCIÓN: IMÁGENES DEL CALENDARIO ---
    fun obtenerImagenCumpleanios(dia: Int, temporadaIndex: Int): Int? {
        if (temporadaIndex != 0) return null // Solo Primavera por ahora

        return when (dia) {
            7 -> R.drawable.lewis_calendario
            10 -> R.drawable.vicent_calendario
            14 -> R.drawable.harley_calendario
            18 -> R.drawable.pam_calendario
            20 -> R.drawable.shane_calendario
            26 -> R.drawable.pierre_calendario
            27 -> R.drawable.emely_calendario
            else -> null
        }
    }

    // --- SECCIÓN: LÓGICA DEL POP-UP (JSON + EVENTOS) ---
    fun obtenerContenidoDia(dia: Int, temporadaIndex: Int): ContenidoPopUp {
        val temporada = getNombreTemporada(temporadaIndex)

        // 1. Lógica de Cumpleaños (Aquí conectamos los datos de tu JSON)
        if (temporada == "Primavera") {
            when (dia) {
                27 -> return ContenidoPopUp.Personaje("Emily", listOf("Amatista", "Tela", "Esmeralda"), listOf("Narciso", "Cuarzo"), listOf("Sashimi", "Acebo", "Rollitos maki"))
                26 -> return ContenidoPopUp.Personaje("Pierre", listOf("Calamares fritos", "Catálogo"), listOf("Leche", "Narciso", "Diente de león"), listOf("Maíz", "Ajo", "Peces"))
                20 -> return ContenidoPopUp.Personaje("Shane", listOf("Cerveza", "Pizza", "Chile"), listOf("Leche"), listOf("Peces", "Maíz", "Ajo"))
                // Agrega aquí a los otros personajes de primavera si quieres que abran su info
            }
        }

        // 2. Lógica de Recetas y Eventos Especiales
        if (temporada == "Primavera") {
            if (dia in listOf(2, 10, 17, 24)) {
                return ContenidoPopUp.Evento("¡Receta Nueva!", "Revisa la TV para aprender algo rico.")
            }
            if (dia in 15..17) {
                return ContenidoPopUp.Evento("¡Día de frambuesas!", "Busca en los arbustos, ¡hay muchas!")
            }
            if (dia == 5) {
                return ContenidoPopUp.Evento("Centro Cívico", "Ir al Centro Cívico desde las 8 am hasta las 1 pm.")
            }
        }

        // 3. Si no hay nada especial, mostramos un consejo
        return ContenidoPopUp.Consejo("Colocar consejo")
    }

    // --- SECCIÓN: NAVEGACIÓN ---
    fun siguienteTemporada() {
        if (_temporadaActualIndex.value < 3) {
            _temporadaActualIndex.value++
        }
    }

    fun anteriorTemporada() {
        if (_temporadaActualIndex.value > 0) {
            _temporadaActualIndex.value--
        }
    }

    fun getNombreTemporada(index: Int): String {
        return temporadas[index]
    }
}

