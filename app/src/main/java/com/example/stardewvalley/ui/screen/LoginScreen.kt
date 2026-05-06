package com.example.stardewvalley.ui.screen

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stardewvalley.R
import com.example.stardewvalley.data.AppDatabase
import com.example.stardewvalley.data.Farm

class LoginScreen : AppCompatActivity() {

    private lateinit var adapter: FarmAdapter
    private val listaGranjas = mutableListOf<Farm>()
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loginlayout)

        // 1. Inicializamos la base de datos
        database = AppDatabase.getDatabase(this)

        // 2. Configurar el RecyclerView
        val rv = findViewById<RecyclerView>(R.id.rvFarmList)
        rv.layoutManager = LinearLayoutManager(this)

        // 3. Inicializar el Adapter con las funciones (Lambdas) para los botones
        adapter = FarmAdapter(
            listaGranjas,
            onDeleteClick = { farm ->
                database.farmDao().deleteFarm(farm) // Borra de la BD
                loadFarms() // Refresca la lista
            },
            onEditClick = { farm ->
                showEditFarmDialog(farm) // Abre el pop-up para editar
            }
        )
        rv.adapter = adapter

        // 4. Cargar datos iniciales
        loadFarms()

        // 5. Botón para agregar nueva granja
        val btnAgregar = findViewById<ImageButton>(R.id.btnAddFarm)
        btnAgregar.setOnClickListener {
            showNewFarmDialog()
        }
    }

    private fun loadFarms() {
        val datosGuardados = database.farmDao().getAllFarms()
        listaGranjas.clear()
        listaGranjas.addAll(datosGuardados)
        adapter.notifyDataSetChanged()
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
            if (nombre.isEmpty()) {
                showErrorDialog()
            } else {
                val nuevaGranja = Farm(nombre = nombre)
                database.farmDao().insertFarm(nuevaGranja)
                loadFarms()
                dialog.dismiss()
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
        val btnAceptar = dialog.findViewById<Button>(R.id.btnAceptar)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelar)

        // Pre-cargamos el nombre actual
        input.setText(farm.nombre)

        btnAceptar.setOnClickListener {
            val nuevoNombre = input.text.toString().trim()
            if (nuevoNombre.isEmpty()) {
                showErrorDialog()
            } else {
                // Actualizamos el objeto existente
                val farmActualizada = farm.copy(nombre = nuevoNombre)
                database.farmDao().updateFarm(farmActualizada)
                loadFarms()
                dialog.dismiss()
            }
        }
        btnCancelar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showErrorDialog() {
        val errorDialog = Dialog(this)
        errorDialog.setContentView(R.layout.dialog_new_farm)
        errorDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val errorText = errorDialog.findViewById<TextView>(R.id.etFarmName)
        errorText.setText("¡Debes ingresar un nombre de granja!")
        errorText.isEnabled = false

        val btnOk = errorDialog.findViewById<Button>(R.id.btnAceptar)
        errorDialog.findViewById<Button>(R.id.btnCancelar).visibility = View.GONE

        btnOk.setOnClickListener { errorDialog.dismiss() }
        errorDialog.show()
    }
}