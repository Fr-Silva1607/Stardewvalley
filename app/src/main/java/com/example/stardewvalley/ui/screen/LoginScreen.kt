package com.example.stardewvalley.ui.screen

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loginlayout)

        // 1. Inicializar Base de Datos
        database = AppDatabase.getDatabase(this)

        // 2. Configurar RecyclerView
        val rv = findViewById<RecyclerView>(R.id.rvFarmList)
        rv.layoutManager = LinearLayoutManager(this)

        // 3. Adaptador con Lambdas corregidas
        adapter = FarmAdapter(
            listaGranjas,
            onDeleteClick = { farm -> deleteFarm(farm) },
            onEditClick = { farm -> showEditFarmDialog(farm) }
        )
        rv.adapter = adapter

        // 4. Botón Agregar
        findViewById<ImageButton>(R.id.btnAddFarm).setOnClickListener {
            showNewFarmDialog()
        }

        // 5. Cargar datos al iniciar
        loadFarms()
    }

    // --- LÓGICA DE NAVEGACIÓN ---
    fun irAConfiguracion() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // --- LÓGICA DE BASE DE DATOS (Hilos secundarios) ---
    private fun loadFarms() {
        lifecycleScope.launch(Dispatchers.IO) {
            val datos = database.farmDao().getAllFarms()
            withContext(Dispatchers.Main) {
                listaGranjas.clear()
                listaGranjas.addAll(datos)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun deleteFarm(farm: Farm) {
        lifecycleScope.launch(Dispatchers.IO) {
            database.farmDao().deleteFarm(farm)
            loadFarms() // Recarga después de borrar
        }
    }

    // --- DIÁLOGOS (XML) ---
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
                    database.farmDao().insertFarm(Farm(nombre = nombre))
                    loadFarms()
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