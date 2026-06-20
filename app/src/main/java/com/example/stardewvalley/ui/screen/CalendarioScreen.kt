package com.example.stardewvalley.ui.screen

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.stardewvalley.R
import com.example.stardewvalley.ui.theme.StardewBeige
import com.example.stardewvalley.ui.theme.StardewMarrone
import com.example.stardewvalley.ui.theme.StardewTexto
import com.example.stardewvalley.viewmodel.CalendarioViewModel
import com.example.stardewvalley.viewmodel.ContenidoPopUp
import com.example.stardewvalley.viewmodel.CultivoPlantado
import com.example.stardewvalley.viewmodel.CultivoCargado
import com.example.stardewvalley.viewmodel.CultivoViewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import androidx.compose.runtime.collectAsState

@Composable
fun CalendarioScreen(
    navController: NavController,
    calVM: CalendarioViewModel = viewModel(),
    cultiVM: CultivoViewModel  = viewModel()
) {
    val context = LocalContext.current
    val temporadaIndex    by calVM.temporadaActualIndex.collectAsState()
    val nombreTemporada    = calVM.getNombreTemporada(temporadaIndex)
    val cultivosPlantados by cultiVM.cultivosPlantados.collectAsState(initial = emptyList())
    val todosLosCultivos  by cultiVM.listaProcesadaDelJson.collectAsState()
    val diaActualSimulado by cultiVM.diaActual.collectAsState()
    val energia           by cultiVM.energia.collectAsState()

    var diaSeleccionado by remember { mutableIntStateOf(1) }
    var mostrarPopUp    by remember { mutableStateOf(false) }

    // Estado único para el menú desplegable unificado
    var gestionExpandida by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter      = painterResource(id = R.drawable.fondo),
            contentDescription = null,
            modifier     = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                        AndroidView(factory = { ctx ->
                            LayoutInflater.from(ctx).inflate(R.layout.superior2, null)
                        })
                        
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Año 1", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { calVM.anteriorTemporada() }) {
                                    Image(painterResource(R.drawable.fi_civico_), null, modifier = Modifier.size(30.dp))
                                }
                                Text(nombreTemporada, color = StardewMarrone, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                                IconButton(onClick = { calVM.siguienteTemporada() }) {
                                    Image(painterResource(R.drawable.fd_civico_), null, modifier = Modifier.size(30.dp))
                                }
                            }
                        }
                    }
                }
            }

            // GRID CALENDARIO
            item {
                Box(modifier = Modifier.padding(10.dp).background(Color(0xFF4E2C0A)).padding(4.dp)) {
                    Column {
                        for (fila in 0..3) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (columna in 1..7) {
                                    val dia = (fila * 7) + columna
                                    val fotoCumple = calVM.obtenerImagenCumpleanios(dia, temporadaIndex)
                                    val iconoExtra = if (nombreTemporada == "Primavera" && dia in 15..17) R.drawable.fresa else null

                                    val cultivosCosecha = cultivosPlantados.filter { 
                                        val diasDesdeSiembra = dia - it.diaPlante
                                        if (it.creceDeNuevo > 0) {
                                            if (diasDesdeSiembra == it.diasCrecimiento) true
                                            else if (diasDesdeSiembra > it.diasCrecimiento) (diasDesdeSiembra - it.diasCrecimiento) % it.creceDeNuevo == 0
                                            else false
                                        } else {
                                            if (diasDesdeSiembra > 0 && it.diasCrecimiento > 0 && diasDesdeSiembra % it.diasCrecimiento == 0) {
                                                val numCosecha = diasDesdeSiembra / it.diasCrecimiento
                                                numCosecha <= it.replantar
                                            } else false
                                        }
                                    }
                                    
                                    val cultivosCreciendo = cultivosPlantados.filter {
                                        val diasDesdeSiembra = dia - it.diaPlante
                                        val duracionTotal = if (it.creceDeNuevo > 0) 28 else it.diasCrecimiento * it.replantar
                                        diasDesdeSiembra >= 0 && diasDesdeSiembra <= duracionTotal && !cultivosCosecha.contains(it)
                                    }

                                    Box(modifier = Modifier.weight(1f)) {
                                        DiaCelda(
                                            dia        = dia,
                                            fotoRes    = fotoCumple,
                                            iconoExtra = iconoExtra,
                                            cultivosCosecha = cultivosCosecha,
                                            cultivosCreciendo = cultivosCreciendo,
                                            calVM = calVM,
                                            onClick    = {
                                                diaSeleccionado = dia
                                                mostrarPopUp    = true
                                                cultiVM.setDiaActual(dia)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { navController.navigate("centro_civico") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Check-list", color = Color.White)
                }
            }

            item {
                // UNIFICACIÓN: Planificador + Tabla + Energía en un solo bloque visual
                CardDesplegable(
                    titulo = "GESTIÓN DE CULTIVOS Y ENERGÍA",
                    expandido = gestionExpandida,
                    onToggle = { gestionExpandida = !gestionExpandida },
                    colorFondo = Color(0xFFF5E6D3)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            // Planificador ARRIBA dentro del desplegable
                            SeccionPlanificador(diaActual = diaActualSimulado, viewModel = cultiVM)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = StardewMarrone.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text("TABLA DE SEMILLAS ($nombreTemporada)", fontWeight = FontWeight.Bold, color = StardewMarrone, modifier = Modifier.padding(bottom = 8.dp))
                            TablaCultivos(viewModel = cultiVM, temporada = nombreTemporada)
                        }
                        
                        // Barra de energía unificada al lado
                        Spacer(modifier = Modifier.width(8.dp))
                        BarraEnergiaVertical(energia)
                    }
                }
            }

            item {
                IconButton(
                    onClick = { 
                        val intent = Intent(context, LoginScreen::class.java)
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .size(60.dp)
                ) {
                    Image(
                        painterResource(R.drawable.fi_civico_), 
                        contentDescription = "Retroceder a Login", 
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        if (mostrarPopUp) {
            PopUpEventos(
                dia = diaSeleccionado,
                onDismiss = { mostrarPopUp = false },
                onDiaCambiar = { nuevoDia ->
                    diaSeleccionado = nuevoDia
                    cultiVM.setDiaActual(nuevoDia)
                },
                viewModel = calVM,
                cultiVM = cultiVM
            )
        }
    }
}

@Composable
fun BarraEnergiaVertical(energia: Float) {
    Column(
        modifier = Modifier
            .width(40.dp)
            .height(300.dp) // Más corta según solicitud
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("E", color = StardewMarrone, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Box(
            modifier = Modifier
                .width(18.dp)
                .weight(1f)
                .background(Color(0xFF3E2723), RoundedCornerShape(4.dp))
                .border(2.dp, Color(0xFF21130D), RoundedCornerShape(4.dp))
                .padding(2.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            val colorBarra = when {
                energia > 60 -> Color(0xFF4CAF50)
                energia > 30 -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight((energia / 120f).coerceIn(0f, 1f))
                    .background(colorBarra, RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun CardDesplegable(
    titulo: String,
    expandido: Boolean,
    onToggle: () -> Unit,
    colorFondo: Color,
    contenido: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        border = BorderStroke(2.dp, StardewMarrone)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onToggle() }.fillMaxWidth()
            ) {
                Text(titulo, fontWeight = FontWeight.Bold, color = StardewMarrone, modifier = Modifier.weight(1f))
                Text(if (expandido) "▲" else "▼", color = StardewMarrone, fontWeight = FontWeight.ExtraBold)
            }
            AnimatedVisibility(visible = expandido) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    contenido()
                }
            }
        }
    }
}

@Composable
fun DiaCelda(
    dia: Int,
    fotoRes: Int?,
    iconoExtra: Int?,
    cultivosCosecha: List<CultivoPlantado>,
    cultivosCreciendo: List<CultivoPlantado>,
    calVM: CalendarioViewModel,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.aspectRatio(0.7f).padding(1.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = StardewBeige),
        border = BorderStroke(1.dp, StardewMarrone),
        shape = RoundedCornerShape(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(text = dia.toString(), color = StardewTexto, fontSize = 9.sp, modifier = Modifier.align(Alignment.TopEnd).padding(2.dp))

            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                // ALDEANO ARRIBA
                if (fotoRes != null && fotoRes != 0) {
                    Image(
                        painter = painterResource(id = fotoRes),
                        contentDescription = "Cumpleaños",
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.White, CircleShape)
                    )
                }

                // CULTIVO ABAJO (FASES DEBAJO DE ALDEANOS)
                if (cultivosCosecha.isNotEmpty()) {
                    val cultivo = cultivosCosecha.first()
                    val faseImg = calVM.obtenerImagenFase(cultivo.nombre, dia, cultivo.diaPlante, cultivo.diasCrecimiento, cultivo.creceDeNuevo, cultivo.replantar)
                    val resId = context.resources.getIdentifier(faseImg, "drawable", context.packageName)
                    if (resId != 0) Image(painterResource(resId), null, modifier = Modifier.size(16.dp))
                } else if (cultivosCreciendo.isNotEmpty()) {
                    val cultivo = cultivosCreciendo.first()
                    val faseImg = calVM.obtenerImagenFase(cultivo.nombre, dia, cultivo.diaPlante, cultivo.diasCrecimiento, cultivo.creceDeNuevo, cultivo.replantar)
                    val resId = context.resources.getIdentifier(faseImg, "drawable", context.packageName)
                    if (resId != 0) Image(painterResource(resId), null, modifier = Modifier.size(12.dp).alpha(0.7f))
                }
            }

            if (iconoExtra != null) {
                Image(painterResource(id = iconoExtra), null, modifier = Modifier.size(12.dp).align(Alignment.BottomStart).padding(1.dp))
            }
        }
    }
}

@Composable
fun TablaCultivos(viewModel: CultivoViewModel, temporada: String) {
    val listaDisponible by viewModel.listaProcesadaDelJson.collectAsState()
    val context = LocalContext.current
    val diaActual by viewModel.diaActual.collectAsState()

    val listaFiltrada = listaDisponible.filter { it.estacion.contains(temporada, ignoreCase = true) }

    Column {
        listaFiltrada.forEach { cultivo ->
            val imgNombre = cultivo.imagen.substringBefore(".")
            val resId = context.resources.getIdentifier(imgNombre, "drawable", context.packageName)
            val drawable = ContextCompat.getDrawable(context, if (resId != 0) resId else android.R.drawable.ic_menu_gallery)
            ItemCultivoTabla(
                cultivo = cultivo,
                viewModel = viewModel,
                diaActual = diaActual,
                onCantidadChange = { viewModel.actualizarSeleccion(cultivo.nombre, it) },
                onReplantarChange = { viewModel.actualizarReplantar(cultivo.nombre, it) },
                imagenPainter = rememberDrawablePainter(drawable)
            )
        }
    }
}

@Composable
fun ItemCultivoTabla(
    cultivo: CultivoCargado, 
    viewModel: CultivoViewModel,
    diaActual: Int,
    onCantidadChange: (Int) -> Unit,
    onReplantarChange: (Int) -> Unit,
    imagenPainter: androidx.compose.ui.graphics.painter.Painter
) {
    var cantidad by remember(cultivo.nombre) { mutableIntStateOf(cultivo.cantidad) }
    var replantar by remember(cultivo.nombre) { mutableIntStateOf(cultivo.replantar) }
    
    LaunchedEffect(cultivo.cantidad) { if (cultivo.cantidad == 0) cantidad = 0 }
    LaunchedEffect(cultivo.replantar) { replantar = cultivo.replantar }
    
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(Color(0xFFE1C699).copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp)).padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painter = imagenPainter, contentDescription = null, modifier = Modifier.size(36.dp))
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text(text = cultivo.nombre, color = Color(0xFF4E2C0A), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = "Semilla: $${cultivo.precioSemilla} | Venta: $${cultivo.precioVenta}", color = Color(0xFF7A5C37), style = MaterialTheme.typography.bodySmall)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color(0xFFF5E6D3), shape = RoundedCornerShape(50))) {
                    IconButton(onClick = { if (cantidad > 0) { cantidad--; onCantidadChange(cantidad) } }, modifier = Modifier.size(24.dp)) { Text("-", fontWeight = FontWeight.Bold) }
                    Text(text = "$cantidad", modifier = Modifier.padding(horizontal = 4.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    IconButton(onClick = { cantidad++; onCantidadChange(cantidad) }, modifier = Modifier.size(24.dp)) { Text("+", fontWeight = FontWeight.Bold) }
                }
                
                if (cultivo.creceDeNuevo == 0) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Replantar: ", fontSize = 10.sp, color = Color(0xFF4E2C0A))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color(0xFFBCAAA4), shape = RoundedCornerShape(50))) {
                            IconButton(onClick = { if (replantar > 1) { replantar--; onReplantarChange(replantar) } }, modifier = Modifier.size(18.dp)) { Text("-", fontSize = 10.sp) }
                            Text(text = "$replantar", modifier = Modifier.padding(horizontal = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { replantar++; onReplantarChange(replantar) }, modifier = Modifier.size(18.dp)) { Text("+", fontSize = 10.sp) }
                        }
                    }
                }
            }
        }
        
        if (cantidad > 0) {
            val usoExtra = viewModel.verificarUsoExtra(cultivo.nombre)
            val mejorCant = viewModel.calcularMejorCantidad(cultivo, diaActual)
            val fasesCalc = viewModel.obtenerCalculoFases(cultivo.nombre, diaActual)
            
            Column(modifier = Modifier.padding(top = 4.dp, start = 40.dp)) {
                if (usoExtra != null) {
                    Text(text = usoExtra, color = Color(0xFF1B5E20), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text(text = mejorCant, color = Color(0xFF4E2C0A), fontSize = 10.sp)
                if (fasesCalc.isNotEmpty()) {
                    Text(text = "Fases: $fasesCalc", color = Color(0xFF5D4037), fontSize = 9.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun SeccionPlanificador(diaActual: Int, viewModel: CultivoViewModel) {
    val todosLosCultivos by viewModel.listaProcesadaDelJson.collectAsState()
    val seleccionados = todosLosCultivos.filter { it.cantidad > 0 }
    val energia by viewModel.energia.collectAsState()

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp).background(Color(0xFFE1C699).copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(8.dp)) {
        Text("Planificador de Siembra", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StardewMarrone)
        if (seleccionados.isEmpty()) {
            Text("Selecciona semillas para calcular costes.", color = Color.Gray, fontSize = 12.sp)
        } else {
            seleccionados.forEach { cultivo ->
                val mensaje = viewModel.calcularRentabilidad(cultivo.precioSemilla, cultivo.precioVenta, cultivo.diasCrecimiento, diaActual, cultivo.cantidad, cultivo.replantar, cultivo.creceDeNuevo)
                Text(text = "${cultivo.nombre} (x${cultivo.cantidad}): $mensaje", color = if (mensaje.contains("PERDERÁS")) Color.Red else Color(0xFF2E7D32), fontSize = 11.sp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        val costeTotalEnergia = seleccionados.sumOf { it.cantidad * 4 * (if(it.creceDeNuevo > 0) 1 else it.replantar) }

        Button(
            onClick = { viewModel.guardarCultivosMasivo() },
            enabled = seleccionados.isNotEmpty() && energia >= costeTotalEnergia,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E2C0A))
        ) {
            Text("¡Plantar Todo! (Coste: $costeTotalEnergia energía)", color = Color.White, fontSize = 12.sp)
        }

        if (energia < costeTotalEnergia) {
            Text("No tienes suficiente energía para plantar todo.", color = Color.Red, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
fun PopUpEventos(dia: Int, onDismiss: () -> Unit, onDiaCambiar: (Int) -> Unit, viewModel: CalendarioViewModel, cultiVM: CultivoViewModel) {
    val temporadaIndex by viewModel.temporadaActualIndex.collectAsState()
    val contenido = viewModel.obtenerContenidoDia(dia, temporadaIndex)
    val cultivosPlantados by cultiVM.cultivosPlantados.collectAsState()
    val context = LocalContext.current

    val cultivosDelDia = cultivosPlantados.filter { it ->
        val edad = dia - it.diaPlante
        val duracionTotal = if (it.creceDeNuevo > 0) 28 else it.diasCrecimiento * it.replantar
        edad >= 0 && edad <= duracionTotal
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f), shape = RoundedCornerShape(16.dp), color = Color(0xFFF5E6D3)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (dia > 1) onDiaCambiar(dia - 1) }, enabled = dia > 1) { Image(painterResource(R.drawable.fi_civico_), null, modifier = Modifier.size(30.dp)) }
                    Text("DÍA $dia", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = StardewMarrone)
                    IconButton(onClick = { if (dia < 28) onDiaCambiar(dia + 1) }, enabled = dia < 28) { Image(painterResource(R.drawable.fd_civico_), null, modifier = Modifier.size(30.dp)) }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = StardewMarrone.copy(alpha = 0.5f))
                
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        when (contenido) {
                            is ContenidoPopUp.Consejo -> Text(contenido.texto, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF4E2C0A))
                            is ContenidoPopUp.Evento -> Column { Text(contenido.titulo, fontWeight = FontWeight.Bold, color = StardewMarrone); Text(contenido.descripcion) }
                            is ContenidoPopUp.Personaje -> {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val imgId = viewModel.obtenerImagenCumpleanios(dia, temporadaIndex)
                                        Image(painterResource(if (imgId != null && imgId != 0) imgId else R.drawable.placeholder), null, modifier = Modifier.size(80.dp).clip(CircleShape).border(2.dp, Color(0xFF4E2C0A), CircleShape))
                                        Spacer(Modifier.width(12.dp))
                                        Text(contenido.nombre, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF4E2C0A))
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text("❤ AMADOS", fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                    Text(contenido.ama.joinToString(), fontSize = 12.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text("😊 GUSTOS", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                    Text(contenido.gusta.joinToString(), fontSize = 12.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text("💢 ODIA", fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
                                    Text(contenido.odia.joinToString(), fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    if (cultivosDelDia.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            Text("CULTIVOS EN ESTE DÍA", fontWeight = FontWeight.ExtraBold, color = StardewMarrone, fontSize = 16.sp)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                        items(cultivosDelDia.size) { index ->
                            val c = cultivosDelDia[index]
                            val faseImg = viewModel.obtenerImagenFase(c.nombre, dia, c.diaPlante, c.diasCrecimiento, c.creceDeNuevo, c.replantar)
                            val resId = context.resources.getIdentifier(faseImg, "drawable", context.packageName)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                if (resId != 0) Image(painterResource(resId), null, modifier = Modifier.size(32.dp))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(c.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    val diasRestantes = if (c.creceDeNuevo > 0) {
                                        if (dia - c.diaPlante < c.diasCrecimiento) "Listo en ${c.diasCrecimiento - (dia - c.diaPlante)} días"
                                        else "Produciendo cada ${c.creceDeNuevo} días"
                                    } else {
                                        "Cosecha ${ ( (dia - c.diaPlante) / c.diasCrecimiento ) + 1 } de ${c.replantar}"
                                    }
                                    Text(diasRestantes, fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E2C0A))) { Text("Volver", color = Color.White) }
            }
        }
    }
}

fun normalizarNombre(nombre: String): String =
    nombre.lowercase()
        .replace(" ", "")
        .replace("á", "a")
        .replace("é", "e")
        .replace("í", "i")
        .replace("ó", "o")
        .replace("ú", "u")
        .replace("ñ", "n")
