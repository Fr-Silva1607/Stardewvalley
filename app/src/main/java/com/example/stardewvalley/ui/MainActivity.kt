package com.example.stardewvalley.ui
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stardewvalley.ui.screen.EleccionGranjaScreen
import com.example.stardewvalley.ui.screen.LoginScreen
import com.example.stardewvalley.ui.screen.CalendarioScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("StardewPrefs", MODE_PRIVATE) }

            // 1. Recuperamos el ID de la granja que viene del Login XML
            val farmId = prefs.getInt("selected_farm_id", -1)

            // 2. Revisamos si ESTA granja específica ya eligió bando
            // Buscamos "ruta_1", "ruta_2", etc.
            val bandoDeEstaGranja = remember(farmId) {
                prefs.getString("ruta_$farmId", null)
            }

            // 3. Si bando es null, vamos a elecciones. Si no, directo al calendario.
            val destinoInicial = if (bandoDeEstaGranja == null) "elecciones" else "calendario"

            NavHost(navController = navController, startDestination = destinoInicial) {

                // Pantalla de Elección (Centro Cívico vs Joja)
                composable("elecciones") {
                    EleccionGranjaScreen(navController) {
                        // Al terminar de elegir en el XML inflado, navegamos al calendario
                        navController.navigate("calendario") {
                            // Limpiamos el historial para que no pueda volver a elegir bando
                            popUpTo("elecciones") { inclusive = true }
                        }
                    }
                }

                // Pantalla del Calendario
                composable("calendario") {
                    CalendarioScreen(navController)
                }
            }
        }
    }
}