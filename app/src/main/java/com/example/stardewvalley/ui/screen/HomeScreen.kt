package com.example.stardewvalley.ui.screen
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stardewvalley.R // IMPORTANTE: Importa R de TU proyecto
import com.example.stardewvalley.ui.theme.JojaBlue
import com.example.stardewvalley.ui.theme.StardewBrown
import com.example.stardewvalley.ui.theme.StardewGreen
import com.example.stardewvalley.ui.theme.WhiteSoft

//agregar pop-up diciendo que esto es con partidas nuevas
//notificacion por hora del celular
@Composable
fun HomeScreen(
    onCentroCivicoClick: () -> Unit,
    onMercajogaClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_home),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo del Título (Más grande y más abajo)
            Image(
                painter = painterResource(id = R.drawable.titulo),
                contentDescription = "Stardew Valley Logo",
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(top = 70.dp)
                    .height(500.dp)
            )

            Spacer(modifier = Modifier.weight(0.15f)) // Empuja los botones al centro

            // Botones
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onCentroCivicoClick,
                    modifier = Modifier.fillMaxWidth(0.85f).height(70.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StardewGreen // <--- LLAMADA A COLOR.KT
                    )
                ) {
                    Text("CENTRO CÍVICO", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onMercajogaClick,
                    modifier = Modifier.fillMaxWidth(0.85f).height(70.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JojaBlue // <--- LLAMADA A COLOR.KT
                    )
                ) {
                    Text("MERCAJOJA", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Banner de Spoilers
            Surface(
                color = StardewBrown, // <--- LLAMADA A COLOR.KT
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "¡La guía contiene spoilers!",
                    color = WhiteSoft, // <--- LLAMADA A COLOR.KT
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
