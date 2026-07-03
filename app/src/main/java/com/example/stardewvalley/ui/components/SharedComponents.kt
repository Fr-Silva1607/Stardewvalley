package com.example.stardewvalley.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun TextoGorditoConBorde(
    texto: String, 
    tamanio: Float = 35f, 
    colorRelleno: Color = Color.White, 
    colorBorde: Color = Color(0xFF4E2C0A)
) {
    Box(contentAlignment = Alignment.Center) {
        // Borde del texto (Stroke)
        Text(
            text = texto,
            fontSize = tamanio.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colorBorde,
            style = TextStyle(
                drawStyle = Stroke(
                    width = 6f,
                    join = StrokeJoin.Round
                )
            )
        )
        // Texto principal (Relleno)
        Text(
            text = texto,
            fontSize = tamanio.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colorRelleno
        )
    }
}
