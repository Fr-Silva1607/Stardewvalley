package com.example.stardewvalley.ui.screen

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.stardewvalley.R
import com.example.stardewvalley.data.Farm

class FarmAdapter(
    private val farms: MutableList<Farm>,
    private val onDeleteClick: (Farm) -> Unit, // Función para borrar
    private val onEditClick: (Farm) -> Unit    // Función para editar
) : RecyclerView.Adapter<FarmAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvFarmItemName)
        // Agregamos los botones aquí:
        val btnEditar: ImageButton = view.findViewById(R.id.btnEditFarm)
        val btnBorrar: ImageButton = view.findViewById(R.id.btnDeleteFarm)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_farm, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val farm = farms[position]
        holder.tvNombre.text = farm.nombre

        // Configuramos el botón de Editar (Lápiz)
        holder.btnEditar.setOnClickListener {
            onEditClick(farm)
        }

        // Configuramos el botón de Borrar (Basurero)
        holder.btnBorrar.setOnClickListener {
            onDeleteClick(farm)
        }

        // Clic en todo el cartel (para entrar a la granja después)
        // Dentro de tu FarmAdapter.kt
        // Dentro de FarmAdapter.kt, en onBindViewHolder:
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val prefs = context.getSharedPreferences("StardewPrefs", Context.MODE_PRIVATE)

            with(prefs.edit()) {
                putInt("selected_farm_id", farm.id)
                putString("selected_farm_name", farm.nombre)
                apply()
            }

            // Navegar de XML a la Activity de Compose
            if (context is LoginScreen) {
                context.irAConfiguracion()
            }
        }
    }

    override fun getItemCount() = farms.size
}

@Composable
fun FarmScreen(navController: NavController) { // Quitamos context de aquí para evitar líos
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("StardewPrefs", Context.MODE_PRIVATE)
    val bando = prefs.getString("ruta_elegida", "centro_civico")

    val colorPrincipal = if (bando == "joja") Color(0xFF1E3A5F) else Color(0xFF2D5A27)
    val tituloBando = if (bando == "joja") "Sucursal Joja" else "Centro Cívico"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorPrincipal)
            .padding(16.dp)
    ) {
        Text(text = tituloBando, style = MaterialTheme.typography.headlineLarge, color = Color.White)

        // Aquí podrías meter un botón para volver o para ver tus granjas
    }
}