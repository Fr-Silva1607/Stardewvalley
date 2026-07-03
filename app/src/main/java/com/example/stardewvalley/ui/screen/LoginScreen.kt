package com.example.stardewvalley.ui.screen

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stardewvalley.R
import com.example.stardewvalley.data.AppDatabase
import com.example.stardewvalley.data.Farm
import com.example.stardewvalley.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginScreen : AppCompatActivity() {

    private lateinit var adapter: FarmAdapter
    private val listaGranjas = mutableListOf<Farm>()
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        // Instalamos la Splash Screen antes de llamar a super.onCreate()
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loginlayout)

        database = AppDatabase.getDatabase(this)

        val rv = findViewById<RecyclerView>(R.id.rvFarmList)
        rv.layoutManager = LinearLayoutManager(this)

        adapter = FarmAdapter(
            listaGranjas,
            onDeleteClick = { farm -> deleteFarm(farm) },
            onEditClick = { farm -> showEditFarmDialog(farm) }
        )
        rv.adapter = adapter

        findViewById<ImageButton>(R.id.btnAddFarm).setOnClickListener {
            showNewFarmDialog()
        }

        loadFarms()
    }

    fun irAConfiguracion() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun loadFarms() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val datos = database.farmDao().getAllFarms()
                withContext(Dispatchers.Main) {
                    listaGranjas.clear()
                    listaGranjas.addAll(datos)
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                // El fallbackToDestructiveMigration en AppDatabase se encargará
            }
        }
    }

    private fun deleteFarm(farm: Farm) {
        lifecycleScope.launch(Dispatchers.IO) {
            database.farmDao().deleteFarm(farm)
            // LIMPIEZA: Al borrar la granja, borramos su bando elegido
            val prefs = getSharedPreferences("StardewPrefs", Context.MODE_PRIVATE)
            prefs.edit().remove("ruta_${farm.id}").apply()
            
            loadFarms()
        }
    }

    private fun showNewFarmDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_new_farm)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val input = dialog.findViewById<EditText>(R.id.etFarmName)
        val btnAceptar = dialog.findViewById<Button>(R.id.btnAceptar)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelar)

        btnAceptar.setOnClickListener {
            val nombre = input.text.toString().trim()
            if (nombre.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val newId = database.farmDao().insertFarm(Farm(nombre = nombre))
                    withContext(Dispatchers.Main) {
                        val prefs = getSharedPreferences("StardewPrefs", Context.MODE_PRIVATE)
                        
                        // SOLUCIÓN AL SALTO: Borramos cualquier ruta previa para este ID
                        // (Por si el ID fue usado antes en otra granja borrada)
                        prefs.edit()
                            .remove("ruta_${newId.toInt()}") 
                            .putInt("selected_farm_id", newId.toInt())
                            .putString("selected_farm_name", nombre)
                            .commit() 
                        
                        irAConfiguracion()
                    }
                }
                dialog.dismiss()
            } else {
                showErrorDialog()
            }
        }
        btnCancelar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showEditFarmDialog(farm: Farm) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_new_farm)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val input = dialog.findViewById<EditText>(R.id.etFarmName)
        input.setText(farm.nombre)

        dialog.findViewById<Button>(R.id.btnAceptar).setOnClickListener {
            val nuevoNombre = input.text.toString().trim()
            if (nuevoNombre.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    database.farmDao().updateFarm(farm.copy(nombre = nuevoNombre))
                    loadFarms()
                }
                dialog.dismiss()
            } else {
                showErrorDialog()
            }
        }
        dialog.findViewById<Button>(R.id.btnCancelar).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showErrorDialog() {
        val errorDialog = Dialog(this)
        errorDialog.setContentView(R.layout.dialog_new_farm)
        errorDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val text = errorDialog.findViewById<TextView>(R.id.etFarmName)
        text.text = "¡Debes ingresar un nombre!"
        text.isEnabled = false

        errorDialog.findViewById<Button>(R.id.btnCancelar).visibility = View.GONE
        errorDialog.findViewById<Button>(R.id.btnAceptar).setOnClickListener { errorDialog.dismiss() }
        errorDialog.show()
    }
}
