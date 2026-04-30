package com.example.stardewvalley.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.stardewvalley.R // Importa tus recursos (R)
import com.example.stardewvalley.navigation.Screen
import com.example.stardewvalley.ui.theme.* // Importa tus colores (StardewWood, StardewSkyBlue, etc.)



//cambiar todo a calendario y que el calendario y que tenga un checklist, y ademas que los dias sean
// sean un pop-up
@Composable
fun TemporadasScreen(navController: NavHostController) {
    // Usamos el Box como contenedor base para que el fondo sea celeste
    // y podamos poner las flechas "encima" al final.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StardewSkyBlue)
    ) {
        // --- COLUMNA DE CONTENIDO (Título + Texto + Carteles) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 80.dp, bottom = 100.dp), // MUCHO PADDING TOP para bajar el título
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TÍTULO: Letras grandes y gorditas
            Text(
                text = "Centro Cívico",
                color = StardewWood,
                fontSize = 38.sp,
                fontWeight = FontWeight.Black, // Súper gordita
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // TEXTO DE RECUERDO: Centrado y con aire
            Text(
                text = "¡recuerda que para activar el evento tienes que ir desde el dia 5 de primavera en adelante arriba del pueblo antes de las 10 am!",
                color = Color.Black,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(32.dp)) // Espacio antes de los carteles

            // --- LAS 4 TEMPORADAS VERTICALES ---
            // Las ponemos directo en la columna principal, sin columnas extras
            CartelTemporada(
                titulo = "PRIMAVERA",
                onClick = { navController.navigate(Screen.Primavera.route) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            CartelTemporada(
                titulo = "VERANO",
                onClick = { navController.navigate(Screen.Verano.route) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            CartelTemporada(
                titulo = "OTOÑO",
                onClick = { navController.navigate(Screen.Otono.route) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            CartelTemporada(
                titulo = "INVIERNO",
                onClick = { navController.navigate(Screen.Invierno.route) }
            )
        }

        // --- LAS FLECHAS (ANCLADAS ABAJO) ---
        // Al estar fuera de la Column pero dentro del Box con align(BottomCenter),
        // siempre se verán en el piso de la pantalla.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter) // ESTO LAS RESCATA
                .padding(bottom = 30.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flecha Izquierda (Bajada de 300dp a 80dp porque 300 era una locura jaja)
            Image(
                painter = painterResource(id = R.drawable.fi_civico_),
                contentDescription = "Volver",
                modifier = Modifier
                    .size(80.dp)
                    .clickable {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
            )

            // Flecha Derecha
            Image(
                painter = painterResource(id = R.drawable.fd_civico_),
                contentDescription = "Ir",
                modifier = Modifier
                    .size(80.dp)
                    .clickable {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
            )
        }
    }
}

@Composable
fun CartelTemporada(titulo: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth(0.95f) // Casi todo el ancho para que sea enorme
            .height(100.dp)
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = R.drawable.cartelcivico), // Nombre corregido
            contentDescription = "Cartel de $titulo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Text(
            text = titulo,
            color = Color.Black,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )
    }
}
