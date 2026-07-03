package com.example.stardewvalley.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stardewvalley.data.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CultivoCargado(
    val id              : Int    = 0,
    val nombre          : String = "",
    val imagen          : String = "",
    val precioSemilla   : Int    = 0,
    val diasCrecimiento : Int    = 1,
    val creceDeNuevo    : Int    = 0,
    val multiplicadorCosecha: Int = 1,
    @SerializedName("Venta")
    val venta           : Int    = 0,
    @SerializedName("seVendePor")
    val seVendePor      : Int    = 0,
    val estacion        : String = "",
    var cantidad        : Int    = 0,
    var replantar       : Int    = 1
) {
    val precioVenta: Int get() = if (venta > 0) venta else seVendePor
}

data class CultivoPlantado(
    val nombre: String,
    val cantidad: Int,
    val diaPlante: Int,
    val diasCrecimiento: Int = 12,
    val creceDeNuevo: Int = 0,
    val multiplicadorCosecha: Int = 1,
    val replantar: Int = 1,
    val estacion: String = ""
)

data class BundleItem(val item: String, val bundleName: String)
data class CharacterGift(val nombre: String, val leEncanta: List<String>)

data class CrecimientoInfo(
    val nombre: String,
    val fases: List<String>,
    val duracionFases: List<Int>? = null
)

class CultivoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val cultivoDao = db.cultivoDao()
    private val globalProgressDao = db.globalProgressDao()

    private val _listaProcesadaDelJson = MutableStateFlow<List<CultivoCargado>>(emptyList())
    val listaProcesadaDelJson = _listaProcesadaDelJson.asStateFlow()

    private val _cultivosPlantados = MutableStateFlow<List<CultivoPlantado>>(emptyList())
    val cultivosPlantados = _cultivosPlantados.asStateFlow()

    private val _diaActual = MutableStateFlow(1)
    val diaActual = _diaActual.asStateFlow()

    private val _energia = MutableStateFlow(120f)
    val energia = _energia.asStateFlow()

    private val _dinero = MutableStateFlow(0)
    val dinero = _dinero.asStateFlow()
    
    private val MAX_ENERGIA = 120f
    private var currentFarmId: Int = -1

    private var itemsCentroCivico = mutableListOf<BundleItem>()
    private var gustosPersonajes = mutableListOf<CharacterGift>()
    private var crecimientoData = mutableListOf<CrecimientoInfo>()

    init {
        cargarCultivosDesdeJson()
        cargarUsos()
        cargarCrecimiento()
    }

    fun initFarm(farmId: Int) {
        if (farmId <= 0) {
            Log.e("CultivoVM", "ID de granja inválido: $farmId")
            return
        }
        if (currentFarmId == farmId) return
        currentFarmId = farmId
        viewModelScope.launch(Dispatchers.IO) {
            cargarPartida()
            withContext(Dispatchers.Main) {
                recalcularEnergiaDia()
            }
        }
    }

    private fun cargarCultivosDesdeJson() {
        try {
            val context = getApplication<Application>().applicationContext
            val inputStream = context.assets.open("Cultivo.json")
            val listType = object : TypeToken<List<CultivoCargado>>() {}.type
            _listaProcesadaDelJson.value = Gson().fromJson(inputStream.bufferedReader(), listType)
        } catch (e: Exception) {
            Log.e("CultivoVM", "Error: ${e.message}")
        }
    }

    private fun cargarUsos() {
        try {
            val context = getApplication<Application>().applicationContext
            val ccJson = context.assets.open("centroCivico.json").bufferedReader().use { it.readText() }
            val root = Gson().fromJson<Map<String, Any>>(ccJson, object : TypeToken<Map<String, Any>>() {}.type)
            val cc = root["centro_civico"] as Map<String, Any>
            cc.forEach { (_, salaObj) ->
                val sala = salaObj as Map<String, Any>
                val lotes = sala["lotes"] as List<Map<String, Any>>
                lotes.forEach { lote ->
                    val nombreLote = lote["nombre"] as String
                    val elementos = lote["elementos"] as List<Map<String, String>>
                    elementos.forEach { elem ->
                        itemsCentroCivico.add(BundleItem(elem["item"] ?: "", nombreLote))
                    }
                }
            }
            val pJson = context.assets.open("Personajes.json").bufferedReader().use { it.readText() }
            val pList = Gson().fromJson<List<Map<String, Any>>>(pJson, object : TypeToken<List<Map<String, Any>>>() {}.type)
            pList.forEach { p ->
                val nombre = p["nombre"] as String
                val gustos = p["gustos"] as Map<String, Any>
                val leEncanta = gustos["leEncanta"] as? List<String> ?: emptyList()
                gustosPersonajes.add(CharacterGift(nombre, leEncanta))
            }
        } catch (e: Exception) {
            Log.e("CultivoVM", "Error usos: ${e.message}")
        }
    }

    private fun cargarCrecimiento() {
        try {
            val context = getApplication<Application>().applicationContext
            val inputStream = context.assets.open("crecimiento.json")
            val listType = object : TypeToken<List<CrecimientoInfo>>() {}.type
            crecimientoData = Gson().fromJson(inputStream.bufferedReader(), listType)
        } catch (e: Exception) {
            Log.e("CultivoVM", "Error crecimiento: ${e.message}")
        }
    }

    private suspend fun cargarPartida() {
        if (currentFarmId == -1) return
        val entities = cultivoDao.getCultivosByFarm(currentFarmId)
        val plantados = entities.map {
            CultivoPlantado(it.nombre, it.cantidad, it.diaPlante, it.diasCrecimiento, it.creceDeNuevo, it.multiplicadorCosecha, it.replantar, it.estacion)
        }
        val global = globalProgressDao.getGlobalProgress(currentFarmId)
        
        withContext(Dispatchers.Main) {
            _cultivosPlantados.value = plantados
            _diaActual.value = global?.diaActual ?: 1
            _dinero.value = global?.dineroActual ?: 0
        }
    }

    private fun guardarPartida() {
        if (currentFarmId == -1) return
        viewModelScope.launch(Dispatchers.IO) {
            globalProgressDao.saveGlobalProgress(
                GlobalProgressEntity(currentFarmId, _diaActual.value, _dinero.value)
            )
        }
    }

    fun setDiaActual(nuevoDia: Int) {
        _diaActual.value = nuevoDia
        guardarPartida()
        recalcularEnergiaDia()
    }

    fun setDinero(nuevoDinero: Int) {
        _dinero.value = nuevoDinero
        guardarPartida()
    }

    private fun recalcularEnergiaDia() {
        var mantenimientoTotal = 0
        _cultivosPlantados.value.forEach { it ->
            val edad = _diaActual.value - it.diaPlante
            val duracionTotal = if (it.creceDeNuevo > 0) {
                28 - it.diaPlante
            } else {
                it.diasCrecimiento * it.replantar
            }
            
            if (edad > 0 && edad <= duracionTotal) {
                mantenimientoTotal += it.cantidad * 2
            }
        }
        _energia.value = (MAX_ENERGIA - mantenimientoTotal).coerceAtLeast(0f)
    }

    fun actualizarSeleccion(nombre: String, nuevaCantidad: Int) {
        _listaProcesadaDelJson.value = _listaProcesadaDelJson.value.map {
            if (it.nombre == nombre) it.copy(cantidad = nuevaCantidad) else it
        }
    }

    fun actualizarReplantar(nombre: String, veces: Int) {
        _listaProcesadaDelJson.value = _listaProcesadaDelJson.value.map {
            if (it.nombre == nombre) it.copy(replantar = veces) else it
        }
    }

    fun guardarCultivosMasivo() {
        if (currentFarmId == -1) {
            Log.e("CultivoVM", "Error: Intento de guardar cultivos sin una granja cargada.")
            return
        }

        val seleccionados = _listaProcesadaDelJson.value.filter { it.cantidad > 0 }
        if (seleccionados.isEmpty()) return

        val consumo = seleccionados.sumOf { it.cantidad * 4 * (if(it.creceDeNuevo > 0) 1 else it.replantar) }
        _energia.value = (_energia.value - consumo).coerceAtLeast(0f)
        
        val nuevos = seleccionados.map {
            CultivoPlantado(it.nombre, it.cantidad, _diaActual.value, it.diasCrecimiento, it.creceDeNuevo, it.multiplicadorCosecha, it.replantar, it.estacion)
        }
        
        _cultivosPlantados.value = _cultivosPlantados.value + nuevos
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val entities = seleccionados.map {
                    CultivoPlantadoEntity(
                        farmId = currentFarmId,
                        nombre = it.nombre,
                        cantidad = it.cantidad,
                        diaPlante = _diaActual.value,
                        diasCrecimiento = it.diasCrecimiento,
                        creceDeNuevo = it.creceDeNuevo,
                        multiplicadorCosecha = it.multiplicadorCosecha,
                        replantar = it.replantar,
                        estacion = it.estacion
                    )
                }
                cultivoDao.insertCultivos(entities)
                guardarPartida()
            } catch (e: Exception) {
                Log.e("CultivoVM", "Error fatal al insertar cultivos: ${e.message}")
            }
        }
        
        _listaProcesadaDelJson.value = _listaProcesadaDelJson.value.map { it.copy(cantidad = 0, replantar = 1) }
    }

    fun calcularRentabilidad(precioSem: Int, precioVen: Int, diasCrec: Int, diaAct: Int, cant: Int, replantar: Int, creceDeNuevo: Int): String {
        val diasRes = 28 - diaAct
        val duracionTotal = if (creceDeNuevo > 0) diasRes else diasCrec * replantar
        
        if (duracionTotal > diasRes && creceDeNuevo == 0) return "¡PERDERÁS $${precioSem * cant * replantar}! No crece."
        
        val numCosechas = if (creceDeNuevo > 0) {
            1 + (diasRes - diasCrec) / creceDeNuevo
        } else {
            replantar
        }
        
        val costoTotal = precioSem * cant * (if (creceDeNuevo > 0) 1 else replantar)
        val neto = (precioVen * cant * numCosechas) - costoTotal
        
        return "Costo: $$costoTotal | Neto: $$neto"
    }

    fun verificarUsoExtra(nombre: String): String? {
        itemsCentroCivico.find { it.item.contains(nombre, ignoreCase = true) }?.let { return "¡Guarda 1! Sirve para el Centro Cívico: ${it.bundleName}" }
        gustosPersonajes.find { it.leEncanta.any { gift -> gift.contains(nombre, ignoreCase = true) } }?.let { return "¡Guarda 1! Le encanta a ${it.nombre}" }
        return null
    }

    fun calcularMejorCantidad(cultivo: CultivoCargado, diaActual: Int): String {
        val diasRes = 28 - diaActual
        if (cultivo.diasCrecimiento > diasRes) return "No hay tiempo para cosechar."
        var numCosechas = 1
        if (cultivo.creceDeNuevo > 0) numCosechas += (diasRes - cultivo.diasCrecimiento) / cultivo.creceDeNuevo
        else numCosechas = cultivo.replantar
        
        val multi = if (cultivo.multiplicadorCosecha > 0) cultivo.multiplicadorCosecha else 1
        val costoBase = if (cultivo.creceDeNuevo > 0) cultivo.precioSemilla else cultivo.precioSemilla * cultivo.replantar
        val beneficioUnidad = (cultivo.precioVenta * multi * numCosechas) - costoBase
        val cantEnergia = (MAX_ENERGIA / 4).toInt()
        return "La mejor cantidad de ${cultivo.nombre} es de $cantEnergia. (Beneficio: $$beneficioUnidad c/u, $numCosechas cosechas)"
    }

    fun obtenerCalculoFases(nombre: String, diaPlante: Int): String {
        val info = crecimientoData.find { it.nombre.contains(nombre, ignoreCase = true) } ?: return ""
        val duraciones = info.duracionFases ?: return "Total: 12 días"
        
        val sb = StringBuilder()
        duraciones.forEachIndexed { i, d ->
            sb.append("Fase ${i+1}: $d ${if(d==1) "día" else "días"} | ")
        }
        sb.append("Total: ${duraciones.sum()} días")
        return sb.toString()
    }

    fun obtenerMejorOpcion(todos: List<CultivoCargado>, dia: Int): String {
        val mejor = todos.filter { (28 - dia) >= it.diasCrecimiento }.maxByOrNull { 
            val num = if (it.creceDeNuevo > 0) (1 + (28 - dia - it.diasCrecimiento) / it.creceDeNuevo) else 1
            ((it.precioVenta * (if(it.multiplicadorCosecha>0) it.multiplicadorCosecha else 1) * num) - it.precioSemilla)
        }
        return mejor?.let { "${it.nombre}: Rentabilidad máx." } ?: "Ninguno viable"
    }
}
