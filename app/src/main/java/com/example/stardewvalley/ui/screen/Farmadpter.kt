package com.example.stardewvalley.ui.screen

import android.content.Context
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
    private val onDeleteClick: (Farm) -> Unit,
    private val onEditClick: (Farm) -> Unit
) : RecyclerView.Adapter<FarmAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvFarmItemName)
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

        holder.btnEditar.setOnClickListener {
            onEditClick(farm)
        }

        holder.btnBorrar.setOnClickListener {
            onDeleteClick(farm)
        }

        // Al hacer clic en el cartel de la granja
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val prefs = context.getSharedPreferences("StardewPrefs", Context.MODE_PRIVATE)

            // Guardamos el ID de forma síncrona (commit) para que MainActivity lo lea bien al arrancar
            prefs.edit()
                .putInt("selected_farm_id", farm.id)
                .putString("selected_farm_name", farm.nombre)
                .commit()

            if (context is LoginScreen) {
                context.irAConfiguracion()
            }
        }
    }

    override fun getItemCount() = farms.size
}
