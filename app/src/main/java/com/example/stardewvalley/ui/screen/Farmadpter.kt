package com.example.stardewvalley.ui.screen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
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
        holder.itemView.setOnClickListener {
            // Lógica futura: entrar al juego
        }
    }

    override fun getItemCount() = farms.size
}