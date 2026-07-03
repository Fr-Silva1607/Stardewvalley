package com.example.stardewvalley.ui.screen

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.stardewvalley.R
import com.example.stardewvalley.ui.components.TextoGorditoConBorde
import com.example.stardewvalley.ui.theme.StardewTexto
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun EleccionGranjaScreen(navController: NavController, onFinalizarEleccion: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("StardewPrefs", Context.MODE_PRIVATE) }
    val farmId = prefs.getInt("selected_farm_id", -1)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(140.dp))

            // RUTA CENTRO CÍVICO
            BotonRuta(
                texto = "Centro Cívico",
                fondoRes = R.drawable.cartel,
                onClick = {
                    if (farmId != -1) {
                        prefs.edit().putString("ruta_$farmId", "centro_civico").commit()
                        onFinalizarEleccion()
                    }
                }
            )

            Spacer(modifier = Modifier.height(30.dp))

            // RUTA JOJA
            BotonRuta(
                texto = "MercaJoja",
                fondoRes = R.drawable.cartel_mercajoja,
                onClick = {
                    if (farmId != -1) {
                        prefs.edit().putString("ruta_$farmId", "mercajoja").commit()
                        onFinalizarEleccion()
                    }
                }
            )
        }
    }
}

@Composable
fun BotonRuta(texto: String, fondoRes: Int, onClick: () -> Unit) {
    val context = LocalContext.current
    val drawable = remember(fondoRes) { ContextCompat.getDrawable(context, fondoRes) }

    Box(
        modifier = Modifier
            .width(320.dp)
            .height(130.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberDrawablePainter(drawable),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        // Usamos el color café oscuro de Stardew Valley para el texto
        TextoGorditoConBorde(
            texto = texto,
            colorRelleno = StardewTexto,
            colorBorde = Color(0xFFF3D091) // Color beige claro para el borde (estilo cartel)
        )
    }
}
