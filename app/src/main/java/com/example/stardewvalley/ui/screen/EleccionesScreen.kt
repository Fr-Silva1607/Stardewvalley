package com.example.stardewvalley.ui.screen

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.stardewvalley.R

@Composable
fun EleccionGranjaScreen(navController: NavController, onFinalizarEleccion: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("StardewPrefs", Context.MODE_PRIVATE) }
    val farmId = prefs.getInt("selected_farm_id", -1)

    Box(modifier = Modifier.fillMaxSize()) {

        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // Botones centrados verticalmente
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(140.dp))

            BotonRuta(
                texto = "Centro Cívico",
                fondoRes = R.drawable.cartel,
                onClick = {
                    if (farmId != -1) {
                        prefs.edit().putString("ruta_$farmId", "centro_civico").apply()
                        onFinalizarEleccion()
                    }
                }
            )

            Spacer(modifier = Modifier.height(30.dp))

            BotonRuta(
                texto = "MercaJoja",
                fondoRes = R.drawable.cartel_mercajoja,
                onClick = {
                    navController.navigate("joja_screen")
                }
            )
        }
    }
}

// ─────────────────────────────────────────────
// COMPONENTES COMPARTIDOS (Se usan también en MercaJojaScreen)
// ─────────────────────────────────────────────

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

        TextoGorditoConBorde(texto)
    }
}

@Composable
fun TextoGorditoConBorde(texto: String, tamanio: Float = 95f) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        drawIntoCanvas { canvas ->

            val paintBorde = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = tamanio / 5f
                color = android.graphics.Color.WHITE
                textSize = tamanio
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            val paintRelleno = Paint().apply {
                style = Paint.Style.FILL
                color = android.graphics.Color.BLACK
                textSize = tamanio
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            val x = size.width / 2
            val y = (size.height / 2) - ((paintRelleno.descent() + paintRelleno.ascent()) / 2)

            canvas.nativeCanvas.drawText(texto, x, y, paintBorde)
            canvas.nativeCanvas.drawText(texto, x, y, paintRelleno)
        }
    }
}

@Composable
fun rememberDrawablePainter(drawable: Drawable?): Painter {
    return remember(drawable) {
        object : Painter() {
            override val intrinsicSize: Size
                get() = drawable?.let {
                    Size(it.intrinsicWidth.toFloat(), it.intrinsicHeight.toFloat())
                } ?: Size.Unspecified

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
