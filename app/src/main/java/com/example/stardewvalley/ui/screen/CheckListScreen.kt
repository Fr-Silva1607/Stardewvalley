package com.example.stardewvalley.ui.screen

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.stardewvalley.R
import com.example.stardewvalley.ui.components.TextoGorditoConBorde
import com.example.stardewvalley.viewmodel.CheckListViewModel
import com.example.stardewvalley.viewmodel.ElementoLote
import com.example.stardewvalley.viewmodel.Lote
import com.example.stardewvalley.viewmodel.Sala

@Composable
fun CheckListScreen(navController: NavController, viewModel: CheckListViewModel = viewModel()) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("StardewPrefs", Context.MODE_PRIVATE) }
    val farmId = remember { prefs.getInt("selected_farm_id", -1) }
    
    val salas by viewModel.salas.collectAsState()
    var nombreSalaSeleccionada by remember { mutableStateOf<String?>(null) }
    val salaSeleccionada = salas.find { it.nombre == nombreSalaSeleccionada }

    LaunchedEffect(farmId) {
        viewModel.cargarDatos(farmId)
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF2E5A2E))) { // Color base verde oscuro
        
        // Simulación de rayas verticales del fondo
        Row(modifier = Modifier.fillMaxSize()) {
            repeat(20) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .background(if (it % 2 == 0) Color.Black.copy(alpha = 0.05f) else Color.Transparent)
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Cabecera
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.superior),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                TextoGorditoConBorde(
                    texto = if (salaSeleccionada == null) "CENTRO CÍVICO" else salaSeleccionada.nombre.uppercase(),
                    tamanio = 24f,
                    colorRelleno = Color.White,
                    colorBorde = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de contenido
            Box(modifier = Modifier.weight(1f)) {
                if (salas.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Cargando...", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        if (salaSeleccionada == null) {
                            items(salas) { sala ->
                                SalaResumenItem(sala, onClick = { nombreSalaSeleccionada = sala.nombre })
                            }
                        } else {
                            items(salaSeleccionada.lotes) { lote ->
                                LoteContenedorItem(farmId, salaSeleccionada.nombre, lote, viewModel)
                            }
                        }
                    }
                }
            }

            // Botón atrás (Flecha) - Usando Box con clickable para evitar el componente IconButton
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(60.dp)
                    .clickable {
                        if (nombreSalaSeleccionada != null) {
                            nombreSalaSeleccionada = null
                        } else {
                            navController.popBackStack()
                        }
                    }
            ) {
                Image(
                    painterResource(R.drawable.fi_civico_),
                    contentDescription = "Volver",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun SalaResumenItem(sala: Sala, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD7B899)), // Beige de la imagen
        border = BorderStroke(2.dp, Color(0xFF7A5C37)), // Borde marrón
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    sala.nombre.uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = Color(0xFF3F2E1E), // Marrón oscuro
                    textAlign = TextAlign.Center
                )
                Text(
                    "Recompensa: ${sala.recompensa_zona}",
                    fontSize = 11.sp,
                    color = Color(0xFF7A5C37),
                    textAlign = TextAlign.Center
                )
            }
            // Añadiendo la flecha fi_civico_ al final de la tarjeta de sala (rotada 180 para apuntar a la derecha)
            Image(
                painter = painterResource(R.drawable.fi_civico_),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(180f)
            )
        }
    }
}

@Composable
fun LoteContenedorItem(farmId: Int, nombreSala: String, lote: Lote, viewModel: CheckListViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF4A634A).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                lote.nombre.uppercase(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                "Recompensa: ${lote.recompensa}",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            lote.elementos.forEach { elemento ->
                ItemCheckVisual(farmId, nombreSala, lote.nombre, elemento, viewModel)
            }
        }
    }
}

@Composable
fun ItemCheckVisual(
    farmId: Int,
    nombreSala: String,
    nombreLote: String,
    elemento: ElementoLote,
    viewModel: CheckListViewModel
) {
    val context = LocalContext.current
    val imageResId = remember(elemento.item, elemento.imagen) {
        val itemName = elemento.item.lowercase()
        val mappedName = when {
            itemName.contains("huevo xxl") -> "huevoxxlblanco"
            itemName.contains("concha de nautillo") -> "contanautillo"
            itemName.contains("ciruela salvaje") || itemName.contains("cireula salvaje") -> "ciruelasalvaje"
            else -> elemento.imagen.substringBefore(".")
        }
        
        val id = context.resources.getIdentifier(mappedName, "drawable", context.packageName)
        if (id != 0) id else R.drawable.error
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.toggleElemento(farmId, nombreSala, nombreLote, elemento) }
            .padding(vertical = 6.dp)
    ) {
        Image(
            painter = painterResource(imageResId),
            contentDescription = null,
            modifier = Modifier.size(38.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                elemento.item,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (elemento.detalles.isNotEmpty()) {
                Text(
                    elemento.detalles,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 12.sp
                )
            }
        }
        
        // Checkbox a la derecha (implementado con Box base)
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(
                    if (elemento.completado) Color(0xFF4CAF50) else Color.White, 
                    RoundedCornerShape(4.dp)
                )
                .border(1.5.dp, Color.White, RoundedCornerShape(4.dp))
        )
    }
}
