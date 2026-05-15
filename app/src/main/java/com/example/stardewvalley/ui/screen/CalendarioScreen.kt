package com.example.stardewvalley.ui.screen

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stardewvalley.viewmodel.CalendarioViewModel
import com.example.stardewvalley.R
import com.example.stardewvalley.ui.theme.StardewBeige
import com.example.stardewvalley.ui.theme.StardewMarrone
import com.example.stardewvalley.ui.theme.StardewTexto
import com.example.stardewvalley.viewmodel.ContenidoPopUp
import com.example.stardewvalley.viewmodel.CultivoPlantado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import com.example.stardewvalley.viewmodel.CultivoCargado
import com.example.stardewvalley.viewmodel.CultivoViewModel


// --- 1. MODELOS DE DATOS ---
@Composable
fun CalendarioScreen(
    navController: NavController,
    calVM: CalendarioViewModel = viewModel<CalendarioViewModel>(),
    cultiVM: CultivoViewModel = viewModel<CultivoViewModel>()
) {
    // 1. Estados de los ViewModels (RECOLECTAR TODO AQUÍ)
    val temporadaIndex by calVM.temporadaActualIndex.collectAsState()
    val nombreTemporada = calVM.getNombreTemporada(temporadaIndex)

    // Estos son los que te daban error por estar faltando:
    val cultivosPlantados by cultiVM.cultivosPlantados.collectAsState(initial = emptyList())
    val todosLosCultivos by cultiVM.listaProcesadaDelJson.collectAsState()
    val diaActualSimulado by cultiVM.diaActual.collectAsState()

    var diaSeleccionado by remember { mutableIntStateOf(1) }

    // 2. Estados locales para UI
    var mostrarPopUp by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {

            // --- CABECERA ---
            item {
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
                            Text(nombreTemporada, color = Color(0xFFFFEB3B), style = MaterialTheme.typography.headlineMedium)
                            IconButton(onClick = { calVM.siguienteTemporada() }) {
                                Image(painterResource(R.drawable.fd_civico_), null, modifier = Modifier.size(30.dp))
                            }
                        }
                    }
                }
            }

            // --- GRID DEL CALENDARIO ---
            item {
                Box(modifier = Modifier.padding(10.dp).background(Color(0xFF4E2C0A)).padding(4.dp)) {
                    Column {
                        for (fila in 0..3) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (columna in 1..7) {
                                    val dia = (fila * 7) + columna
                                    val fotoCumple = calVM.obtenerImagenCumpleanios(dia, temporadaIndex)
                                    val iconoTemporada = if (nombreTemporada == "Primavera" && dia in 15..17) R.drawable.fresa else null

                                    Box(modifier = Modifier.weight(1f)) {
                                        DiaCelda(
                                            dia = dia,
                                            fotoRes = fotoCumple,
                                            iconoExtra = iconoTemporada,
                                            onClick = {
                                                diaSeleccionado = dia
                                                mostrarPopUp = true
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

            // --- DETALLES Y TABLA ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5E6D3)),
                    border = BorderStroke(2.dp, StardewMarrone)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("DETALLES DEL DÍA $diaSeleccionado", fontWeight = FontWeight.Bold, color = StardewMarrone)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        TablaCultivos(viewModel = cultiVM)
                    }
                }
            }

            // --- CALCULADORA DE GANANCIA ---
            item {
                val sugerencia = cultiVM.obtenerMejorOpcion(todosLosCultivos, diaActualSimulado)
                Card(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5E6D3))
                ) {
                    Text(
                        text = " $sugerencia",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // --- TIENDA Y HUERTO ---
            item { SeccionPlanificador(diaActual = diaActualSimulado, viewModel = cultiVM) }

            item {
                if (cultivosPlantados.isNotEmpty()) {
                    Text(
                        "Crecimiento en Tiempo Real (Día $diaActualSimulado)",
                        modifier = Modifier.padding(16.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp)
                    ) {
                        cultivosPlantados.forEach { cultivo ->
                            CrecimientoVisualItem(cultivo, diaActualSimulado, cultiVM)
                        }
                    }
                }
            }
        } // Fin LazyColumn

        // PopUp de eventos
        if (mostrarPopUp) {
            PopUpEventos(
                dia = diaSeleccionado,
                onDismiss = { mostrarPopUp = false },
                onDiaCambiar = { nuevoDia ->
                    diaSeleccionado = nuevoDia
                    cultiVM.setDiaActual(nuevoDia)
                },
                viewModel = calVM
            )
        }
    }
}

@Composable
 fun CrecimientoVisualItem(cultivo: CultivoPlantado, diaActual: Int, viewModel: CultivoViewModel) {
    val context = LocalContext.current

    // IMPORTANTE: Asegúrate de que obtenerImagenFase acepte estos 4 parámetros en tu CultivoViewModel
    val nombreImagen = viewModel.obtenerImagenFase(
        nombre = cultivo.nombre,
        diaPlante = cultivo.diaPlante,
        diaActual = diaActual,
        diasTotales = 12
    )

    val resId = context.resources.getIdentifier(nombreImagen, "drawable", context.packageName)
    val drawable = ContextCompat.getDrawable(context, if(resId != 0) resId else android.R.drawable.ic_menu_report_image)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp).width(80.dp)
    ) {
        Image(
            painter = rememberDrawablePainter(drawable),
            contentDescription = null,
            modifier = Modifier.size(60.dp)
        )
        Text(cultivo.nombre, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}
@Composable
fun SeccionCultivos(diaActual: Int, viewModel: CultivoViewModel = viewModel()) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(StardewBeige, RoundedCornerShape(8.dp))
            .border(3.dp, StardewMarrone, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text("Planificador de Siembra", color = StardewTexto, style = MaterialTheme.typography.headlineSmall)

        // Tabla de Cultivos disponibles en el JSON
        Row(modifier = Modifier.fillMaxWidth().background(StardewMarrone).padding(4.dp)) {
            Text("Cultivo", color = Color.White, modifier = Modifier.weight(1f))
            Text("Precio", color = Color.White, modifier = Modifier.weight(0.5f))
            Text("Cantidad", color = Color.White, modifier = Modifier.weight(1f))
        }
    }
}
@Composable
fun ListaSeguimientoCultivos(
    diaActual: Int,
    cultivosPlantados: List<CultivoPlantado>,
    viewModel: CultivoViewModel
) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp)) {
        cultivosPlantados.forEach { cultivo ->
            // 1. Obtenemos el nombre del archivo desde el ViewModel (ej: "fase3_coliflor")
            // Usamos 12 como ejemplo de días totales de crecimiento
            val nombreImagen = viewModel.obtenerImagenFase(cultivo.nombre, cultivo.diaPlante, diaActual, 12)

            // 2. Buscamos el ID del recurso dinámicamente
            val drawableId = context.resources.getIdentifier(nombreImagen, "drawable", context.packageName)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(8.dp).background(StardewBeige)
            ) {
                // 3. Mostramos la imagen de la fase actual
                Image(
                    painter = painterResource(id = if (drawableId != 0) drawableId else R.drawable.placeholder),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )

                Text(
                    text = "${cultivo.nombre} (x${cultivo.cantidad})",
                    color = StardewTexto,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
@Composable
fun SeguimientoCrecimiento(diaActual: Int, cultivos: List<CultivoPlantado>) {
    if (cultivos.isEmpty()) return // No se muestra nada si no hay nada plantado

    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(StardewBeige)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text("Tus Plantaciones:", color = StardewTexto)
            cultivos.forEach { cultivo ->
                val diasPasados = diaActual - cultivo.diaPlante
                val faltan = 12 - diasPasados // Ejemplo con Coliflor (12 días)

                if (faltan >= 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${cultivo.nombre}: Faltan $faltan días", color = StardewTexto)
                        // Aquí podrías cambiar la imagen según el día (fase de crecimiento)
                    }
                }
            }
        }
    }
}
@Composable
fun ItemCultivoTabla(
    nombre: String,
    precio: Int,
    onCantidadChange: (Int) -> Unit,
    // Recibimos el Painter directamente (el objeto, no la función)
    rememberDrawablePainter: Painter
) {
    // Estado local para la cantidad en esta fila
    var cantidad by remember { mutableIntStateOf(0) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color(0xFFF5E6D3), shape = RoundedCornerShape(8.dp)) // Fondo crema suave
            .padding(8.dp)
    ) {
        // 1. Imagen del cultivo usando el Painter que llega por parámetro
        Image(
            painter = rememberDrawablePainter,
            contentDescription = "Imagen de $nombre",
            modifier = Modifier
                .size(48.dp)
                .padding(4.dp)
        )

        // 2. Información del cultivo (Nombre y Precio)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = nombre,
                color = Color(0xFF4E2C0A), // StardewTexto
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$$precio c/u",
                color = Color(0xFF7A5C37),
                style = MaterialTheme.typography.bodySmall
            )
        }

        // 3. Controles de cantidad (+ / -)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.background(Color(0xFFE1C699), shape = CircleShape)
        ) {
            IconButton(
                onClick = {
                    if (cantidad > 0) {
                        cantidad--
                        onCantidadChange(cantidad)
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Text("-", color = Color(0xFF4E2C0A), fontWeight = FontWeight.Bold)
            }

            Text(
                text = "$cantidad",
                color = Color(0xFF4E2C0A),
                modifier = Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold
            )

            IconButton(
                onClick = {
                    cantidad++
                    onCantidadChange(cantidad)
                },
                modifier = Modifier.size(32.dp)
            ) {
                Text("+", color = Color(0xFF4E2C0A), fontWeight = FontWeight.Bold)
            }
        }
    }
}
@Composable
fun PopUpEventos(
    dia: Int,
    onDismiss: () -> Unit,
    onDiaCambiar: (Int) -> Unit,
    viewModel: CalendarioViewModel
) {
    val context = LocalContext.current
    val contenido = viewModel.obtenerContenidoDia(dia, viewModel.temporadaActualIndex.collectAsState().value)

    val painterIzquierda = rememberDrawablePainter(ContextCompat.getDrawable(context, R.drawable.fi_civico_))
    val painterDerecha = rememberDrawablePainter(ContextCompat.getDrawable(context, R.drawable.fd_civico_))

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF5E6D3)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // --- CABECERA ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (dia > 1) onDiaCambiar(dia - 1) }, enabled = dia > 1) {
                        Image(painter = painterIzquierda, contentDescription = null, modifier = Modifier.size(44.dp))
                    }
                    Text(text = "DÍA $dia", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF4E2C0A), fontWeight = FontWeight.ExtraBold)
                    IconButton(onClick = { if (dia < 28) onDiaCambiar(dia + 1) }, enabled = dia < 28) {
                        Image(painter = painterDerecha, contentDescription = null, modifier = Modifier.size(44.dp))
                    }
                }

                HorizontalDivider(color = Color(0xFF4E2C0A), thickness = 2.dp, modifier = Modifier.padding(vertical = 8.dp))

                // --- LÓGICA DINÁMICA SEGÚN EL TIPO DE CONTENIDO ---
                when (contenido) {
                    is ContenidoPopUp.Consejo -> {
                        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFE1C699), RoundedCornerShape(8.dp)).padding(12.dp)) {
                            Text(text = contenido.texto, color = Color(0xFF4E2C0A))
                        }
                    }
                    is ContenidoPopUp.Evento -> {
                        Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFB0E0E6), RoundedCornerShape(8.dp)).padding(12.dp)) {
                            Text(text = contenido.titulo, fontWeight = FontWeight.Bold, color = Color(0xFF005073))
                            Text(text = contenido.descripcion, color = Color(0xFF005073))
                        }
                    }
                    is ContenidoPopUp.Personaje -> {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            // Imagen (la buscamos por nombre: "Emily" -> emily)
                            val nombreRes = contenido.nombre.lowercase()
                            val imageRes = context.resources.getIdentifier(nombreRes, "drawable", context.packageName)

                            Image(
                                painter = painterResource(id = if (imageRes != 0) imageRes else R.drawable.placeholder),
                                contentDescription = contenido.nombre,
                                modifier = Modifier.size(70.dp).clip(CircleShape).border(2.dp, Color(0xFF4E2C0A), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = contenido.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF4E2C0A))
                                // Las 3 tablas de gustos desde tu Personaje data class
                                TablaGustos(titulo = "❤ LE ENCANTA", items = contenido.ama)
                                TablaGustos(titulo = "😊 LE GUSTA", items = contenido.gusta)
                                TablaGustos(titulo = "💢 LO ODIA", items = contenido.odia)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E2C0A))
                ) {
                    Text("Volver al Calendario", color = Color.White)
                }
            }
        }
    }
}
@Composable
fun TablaGustos(titulo: String, items: List<String>) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(titulo, style = MaterialTheme.typography.labelLarge, color = StardewMarrone)
        // Solo mostramos hasta 3 ítems como pediste
        items.take(3).forEach { item ->
            Text("• $item", style = MaterialTheme.typography.bodySmall, color = StardewTexto)
        }
    }
}
@Composable
fun DiaCelda(
    dia: Int,
    fotoRes: Int?,
    iconoExtra: Int?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(0.8f)
            .padding(2.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = StardewBeige),
        border = BorderStroke(2.dp, StardewMarrone),
        shape = RoundedCornerShape(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = dia.toString(),
                color = StardewTexto,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
            )

            // Cumpleaños (Lewis, Emily, etc.)
            if (fotoRes != null) {
                Image(
                    painter = painterResource(id = fotoRes),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp).align(Alignment.Center)
                )
            }

            // Eventos de temporada (Frambuesas)
            if (iconoExtra != null) {
                Image(
                    painter = painterResource(id = iconoExtra),
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun SeccionPlanificador(diaActual: Int, viewModel: CultivoViewModel) {
    val todosLosCultivos by viewModel.listaProcesadaDelJson.collectAsState()
    val seleccionadosEfectivos = todosLosCultivos.filter { it.cantidad > 0 }

    Column(modifier = Modifier.padding(16.dp).background(Color(0xFFF5E6D3)).padding(12.dp)) {
        Text("Planificador de Siembra", color = Color(0xFF4E2C0A), style = MaterialTheme.typography.titleLarge)

        seleccionadosEfectivos.forEach { cultivo ->
            val mensaje = viewModel.calcularRentabilidad(
                precio = cultivo.precioSemilla,
                venta = cultivo.venta,
                crecimiento = cultivo.diasCrecimiento,
                dia = diaActual,
                cant = cultivo.cantidad
            )

            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text("${cultivo.nombre} (x${cultivo.cantidad})", fontWeight = FontWeight.Bold)
                Text(
                    text = mensaje,
                    color = if (mensaje.contains("PERDERÁS")) Color.Red else Color(0xFF2E7D32)
                )
            }
        }

        // Botón para mover de la tabla al huerto
        Button(onClick = { viewModel.guardarCultivosMasivo() }) {
            Text("Plantar Selección")
        }
    }
}


@Composable
fun PantallaPrincipal(viewModel: CultivoViewModel = viewModel()) {
    // Usamos los nombres de estado exactos de tu ViewModel
    val cultivosPlantados by viewModel.cultivosPlantados.collectAsState()
    val diaActual by viewModel.diaActual.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {

        Text("Tu Huerto en Tiempo Real (Día $diaActual)", style = MaterialTheme.typography.headlineSmall)

        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
            // Filtramos los que realmente están plantados
            cultivosPlantados.forEach { cultivo ->
                // Obtenemos el nombre del recurso (ej: "fase3_chilito")
                val nombreImagen = viewModel.obtenerImagenFase(
                    nombre = cultivo.nombre,
                    diaPlante = cultivo.diaPlante,
                    diaActual = diaActual,
                    diasTotales = 12 // O los días que use tu lógica
                )

                // Buscamos el ID del recurso dinámicamente
                val resId = context.resources.getIdentifier(nombreImagen, "drawable", context.packageName)
                val drawable = ContextCompat.getDrawable(context, if(resId != 0) resId else android.R.drawable.ic_menu_report_image)

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                    // Usamos el helper corregido
                    Image(
                        painter = rememberDrawablePainter(drawable),
                        contentDescription = null,
                        modifier = Modifier.size(60.dp)
                    )
                    Text(cultivo.nombre, style = MaterialTheme.typography.bodySmall)
                    Text("Día ${diaActual - cultivo.diaPlante}", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
@Composable
fun CrecimientoVisualItem(cultivo: CultivoCargado, diaActual: Int) {
    val context = LocalContext.current

    // Suponemos que todos se plantan el día 1 para el simulador,
    // o puedes pasarle el día real de siembra.
    val diaPlante = 1

    // Usamos tu lógica del ViewModel para saber qué fase mostrar (fase1 a fase6)
    val nombreImagen = obtenerNombreFaseManual(cultivo.nombre, diaPlante, diaActual, cultivo.diasCrecimiento)

    val drawableId = remember(nombreImagen) {
        val id = context.resources.getIdentifier(nombreImagen, "drawable", context.packageName)
        if (id != 0) id else R.drawable.placeholder // Placeholder si no encuentra la fase
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(cultivo.nombre, style = MaterialTheme.typography.labelSmall, color = Color(0xFF4E2C0A))

        // Aquí usamos el Image con el ID dinámico que calculamos
        Image(
            painter = painterResource(id = drawableId),
            contentDescription = "Fase de crecimiento de ${cultivo.nombre}",
            modifier = Modifier.size(64.dp)
        )

        // Barra de progreso visual
        val progreso = ((diaActual - diaPlante).toFloat() / cultivo.diasCrecimiento.toFloat()).coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = progreso,
            modifier = Modifier.width(64.dp).height(4.dp),
            color = Color(0xFF2E7D32),
            trackColor = Color.LightGray
        )
    }
}

// Función auxiliar (puedes meterla en el ViewModel o dejarla aquí)
fun obtenerNombreFaseManual(nombre: String, diaPlante: Int, diaActual: Int, diasTotales: Int): String {
    val diasPasados = diaActual - diaPlante
    if (diasPasados >= diasTotales) return "fase6_${nombre.lowercase().replace(" ", "_")}"

    val fase = ((diasPasados.toFloat() / diasTotales.toFloat()) * 5).toInt() + 1
    return "fase${fase}_${nombre.lowercase().replace(" ", "_")}"
}

@Composable
fun TablaCultivos(viewModel: CultivoViewModel) {
    val listaDisponible by viewModel.listaProcesadaDelJson.collectAsState()
    val context = LocalContext.current

    Column {
        listaDisponible.forEach { cultivo ->
            val nombreIcono = "fase1_${cultivo.nombre.lowercase().replace(" ", "_")}"
            val resId = context.resources.getIdentifier(nombreIcono, "drawable", context.packageName)
            val drawable = ContextCompat.getDrawable(context, if(resId != 0) resId else android.R.drawable.ic_menu_gallery)

            ItemCultivoTabla(
                nombre = cultivo.nombre,
                precio = cultivo.precioSemilla,
                onCantidadChange = { nuevaCantidad ->
                    viewModel.actualizarSeleccion(cultivo.nombre, nuevaCantidad)
                },
                // Aquí ya no hay mismatch: pasas un Painter a un parámetro que pide Painter
                rememberDrawablePainter = rememberDrawablePainter(drawable)
            )
        }
    }
}
@Composable
fun AnalisisFinanciero(
    seleccionados: List<CultivoCargado>,
    diaActual: Int, // <-- Agregamos el día como parámetro
    viewModel: CultivoViewModel
) {
    // 1. Creamos las variables locales para asegurar el cálculo
    val listaParaCalculo = seleccionados
    val diaParaCalculo = diaActual

    // 2. Usamos el 'viewModel' que entra por parámetro (no cultiVM)
    val recomendacion = viewModel.obtenerMejorOpcion(listaParaCalculo, diaParaCalculo)
    val presupuesto = 500

    Column(modifier = Modifier.padding(16.dp)) {
        Text("ANÁLISIS DE RENTABILIDAD", fontWeight = FontWeight.Bold, color = Color.White)

        // Mostrar ganancia de lo que el usuario YA seleccionó en la tabla
        seleccionados.filter { it.cantidad > 0 }.forEach { cultivo ->
            val gananciaTotal = (cultivo.venta - cultivo.precioSemilla) * cultivo.cantidad
            Text(
                text = "Si plantas ${cultivo.cantidad} de ${cultivo.nombre}, ganarás: $$gananciaTotal",
                color = Color.White
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // El consejo de la IA para el portafolio
        Text(
            text = "💡 Sugerencia Técnica: $recomendacion",
            color = Color(0xFFBBDEFB), // Un azul claro para que resalte en el fondo oscuro
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


@Composable
fun rememberDrawablePainter(drawable: Drawable?): Painter {
    return remember(drawable) {
        object : Painter() {
            override val intrinsicSize: androidx.compose.ui.geometry.Size
                get() = drawable?.let {
                    androidx.compose.ui.geometry.Size(it.intrinsicWidth.toFloat(), it.intrinsicHeight.toFloat())
                } ?: androidx.compose.ui.geometry.Size.Unspecified

            // Cambiado de 'draw' a 'onDraw' para cumplir con la clase abstracta
            override fun DrawScope.onDraw() {
                drawable?.let {
                    drawIntoCanvas { canvas ->
                        it.setBounds(0, 0, size.width.toInt(), size.height.toInt())
                        it.draw(canvas.nativeCanvas)
                    }
                }
            }
        }
    }
}