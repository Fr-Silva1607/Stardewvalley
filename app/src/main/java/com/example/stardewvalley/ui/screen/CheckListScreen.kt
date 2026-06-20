package com.example.stardewvalley.ui.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.stardewvalley.R
import com.example.stardewvalley.ui.theme.StardewMarrone
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
    var salaSeleccionada by remember { mutableStateOf<Sala?>(null) }

    // Cargar datos específicos de la granja al iniciar
    LaunchedEffect(farmId) {
        viewModel.cargarDatos(farmId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Header con layout superior2
            Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                AndroidView(factory = { ctx ->
                    LayoutInflater.from(ctx).inflate(R.layout.superior2, null)
                })
                Text(
                    text = if (salaSeleccionada == null) "CENTRO CÍVICO" else salaSeleccionada!!.nombre.uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = StardewMarrone,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center).padding(bottom = 10.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Área de contenido scrollable
            Box(modifier = Modifier.weight(1f)) {
                if (salas.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay tareas cargadas. Revisa centrocivico.json", color = Color.White)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        if (salaSeleccionada == null) {
                            items(salas) { sala ->
                                SalaResumenItem(sala, onClick = { salaSeleccionada = sala })
                            }
                        } else {
                            items(salaSeleccionada!!.lotes) { lote ->
                                if (!lote.completado) {
                                    LoteItem(farmId, salaSeleccionada!!.nombre, lote, viewModel)
                                } else {
                                    LoteCompletadoItem(lote.nombre)
                                }
                            }
                        }
                    }
                }
            }

            // Flecha de Volver
            IconButton(
                onClick = {
                    if (salaSeleccionada != null) {
                        salaSeleccionada = null
                    } else {
                        navController.navigate("calendario") {
                            popUpTo("centro_civico") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .padding(16.dp)
                    .size(60.dp)
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5E6D3).copy(alpha = 0.9f)),
        border = BorderStroke(2.dp, StardewMarrone),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    sala.nombre.uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = StardewMarrone,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Recompensa: ${sala.recompensa_zona}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF7A5C37),
                    textAlign = TextAlign.Center
                )
            }
            if (sala.completado) {
                Icon(
                    painter = painterResource(id = android.R.drawable.checkbox_on_background),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color(0xFF4CAF50)
                )
            } else {
                Text("➜", color = StardewMarrone, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }
        }
    }
}

@Composable
fun LoteItem(farmId: Int, nombreSala: String, lote: Lote, viewModel: CheckListViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE1C699).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(lote.nombre.uppercase(), fontWeight = FontWeight.Bold, color = StardewMarrone, textAlign = TextAlign.Center)
        Text("Recompensa: ${lote.recompensa}", fontSize = 12.sp, color = StardewMarrone, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))

        lote.elementos.forEach { elemento ->
            ElementoCheckItem(farmId, nombreSala, lote.nombre, elemento, viewModel)
        }
    }
}

@Composable
fun ElementoCheckItem(
    farmId: Int,
    nombreSala: String,
    nombreLote: String,
    elemento: ElementoLote,
    viewModel: CheckListViewModel
) {
    val context = LocalContext.current
    val imageResId = remember(elemento.imagen) {
        val cleanName = elemento.imagen.substringBefore(".")
        val id = context.resources.getIdentifier(cleanName, "drawable", context.packageName)
        if (id != 0) id else R.drawable.error
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.toggleElemento(farmId, nombreSala, nombreLote, elemento) }
            .padding(vertical = 4.dp)
            .background(if (elemento.completado) Color(0xFFC8E6C9).copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(4.dp))
            .padding(4.dp)
    ) {
        Image(
            painter = painterResource(imageResId),
            contentDescription = elemento.item,
            modifier = Modifier
                .size(32.dp)
                .border(1.dp, StardewMarrone, RoundedCornerShape(4.dp))
                .padding(2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                elemento.item,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (elemento.completado) FontWeight.Normal else FontWeight.Bold,
                color = if (elemento.completado) Color.Gray else StardewMarrone
            )
            Text(elemento.detalles, fontSize = 10.sp, color = Color(0xFF7A5C37))
        }
        
        // RECUADRO INTERACTIVO: Blanco <-> Verde completo (Inmediato)
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    if (elemento.completado) Color(0xFF4CAF50) else Color.White, 
                    RoundedCornerShape(4.dp)
                )
                .border(2.dp, StardewMarrone, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (elemento.completado) {
                Icon(
                    painter = painterResource(id = android.R.drawable.checkbox_on_background),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun LoteCompletadoItem(nombreLote: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF8BC34A).copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = android.R.drawable.checkbox_on_background),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color(0xFF2E7D32)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("$nombreLote - COMPLETADO", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), textAlign = TextAlign.Center)
    }
}
