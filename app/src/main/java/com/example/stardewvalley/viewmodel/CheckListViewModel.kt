package com.example.stardewvalley.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import android.util.Log

data class ElementoLote(
    val item: String,
    val detalles: String,
    val imagen: String,
    val completado: Boolean = false
)

data class Lote(
    val nombre: String,
    val recompensa: String,
    val elementos: List<ElementoLote>,
    val completado: Boolean = false
)

data class Sala(
    val nombre: String,
    val recompensa_zona: String,
    val lotes: List<Lote>,
    val completado: Boolean = false
)

class CheckListViewModel(application: Application) : AndroidViewModel(application) {

    private val _salas = MutableStateFlow<List<Sala>>(emptyList())
    val salas = _salas.asStateFlow()

    private val prefs = application.getSharedPreferences("CheckListPrefs", Context.MODE_PRIVATE)

    fun cargarDatos(farmId: Int) {
        try {
            val context = getApplication<Application>().applicationContext
            val inputStream = context.assets.open("centroCivico.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            
            val rootType = object : TypeToken<Map<String, Map<String, Any>>>() {}.type
            val root: Map<String, Map<String, Any>> = Gson().fromJson(jsonString, rootType)
            val centroCivico = root["centro_civico"] ?: return

            val listaSalas = centroCivico.map { (nombreSala, salaData) ->
                val salaMap = salaData as Map<*, *>
                val recompensaZona = salaMap["recompensa_zona"] as? String ?: ""
                val lotesListRaw = salaMap["lotes"] as? List<*> ?: emptyList<Any>()
                
                val lotes = lotesListRaw.map { loteData ->
                    val loteMap = loteData as Map<*, *>
                    val nombreLote = loteMap["nombre"] as? String ?: ""
                    val recompensaLote = loteMap["recompensa"] as? String ?: ""
                    val elementosListRaw = loteMap["elementos"] as? List<*> ?: emptyList<Any>()
                    
                    val elementos = elementosListRaw.map { elemData ->
                        val elemMap = elemData as Map<*, *>
                        val itemNombre = elemMap["item"] as? String ?: ""
                        val completado = prefs.getBoolean("f${farmId}_${nombreSala}_${nombreLote}_${itemNombre}", false)
                        ElementoLote(
                            item = itemNombre,
                            detalles = elemMap["detalles"] as? String ?: "",
                            imagen = elemMap["imagen"] as? String ?: "error",
                            completado = completado
                        )
                    }
                    
                    val loteCompletado = elementos.isNotEmpty() && elementos.all { it.completado }
                    Lote(nombreLote, recompensaLote, elementos, loteCompletado)
                }

                val salaCompletada = lotes.isNotEmpty() && lotes.all { it.completado }
                Sala(nombreSala, recompensaZona, lotes, salaCompletada)
            }

            _salas.value = listaSalas
        } catch (e: Exception) {
            Log.e("CheckListVM", "Error en cargarDatos: ${e.message}")
        }
    }

    fun toggleElemento(farmId: Int, nombreSala: String, nombreLote: String, elemento: ElementoLote) {
        val completadoNuevo = !elemento.completado
        prefs.edit().putBoolean("f${farmId}_${nombreSala}_${nombreLote}_${elemento.item}", completadoNuevo).apply()
        
        // Actualización reactiva inmediata usando update
        _salas.update { actuales ->
            actuales.map { sala ->
                if (sala.nombre == nombreSala) {
                    val nuevosLotes = sala.lotes.map { lote ->
                        if (lote.nombre == nombreLote) {
                            val nuevosElementos = lote.elementos.map { elem ->
                                if (elem.item == elemento.item) {
                                    elem.copy(completado = completadoNuevo)
                                } else elem
                            }
                            val loteCompletado = nuevosElementos.all { it.completado }
                            lote.copy(elementos = nuevosElementos, completado = loteCompletado)
                        } else lote
                    }
                    val salaCompletada = nuevosLotes.all { it.completado }
                    sala.copy(lotes = nuevosLotes, completado = salaCompletada)
                } else sala
            }
        }
    }
}
