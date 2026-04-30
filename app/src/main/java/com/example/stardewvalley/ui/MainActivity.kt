package com.example.stardewvalley.ui
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stardewvalley.navigation.Screen
import com.example.stardewvalley.ui.screen.CentroCivicoScreen
import com.example.stardewvalley.ui.screen.HomeScreen
import com.example.stardewvalley.ui.screen.MercajojaScreen
import com.example.stardewvalley.ui.screen.TemporadasScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()

                // Dentro de tu NavHost
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route
                ) {
                    // 1. HOME
                    composable(Screen.Home.route) {
                        HomeScreen(
                            onCentroCivicoClick = {
                                // Lógica de memoria para saltar el cartel
                                if (Screen.yaVioTemporadas) {
                                    navController.navigate(Screen.Temporadas.route)
                                } else {
                                    navController.navigate(Screen.CentroCivico.route)
                                }
                            },
                            onMercajogaClick = {
                                navController.navigate(Screen.Mercajoja.route)
                            }
                        )
                    }

                    // 2. CARTEL CENTRO CÍVICO
                    composable(Screen.CentroCivico.route) {
                        CentroCivicoScreen(navController)
                    }

                    // 3. TEMPORADAS (El menú de los 4 carteles verticales)
                    composable(Screen.Temporadas.route) {
                        TemporadasScreen(navController)
                    }

                    // 4. LAS ESTACIONES (Agregamos estas para que no den error al clickear los carteles)
                    composable(Screen.Primavera.route) {
                        // Aquí llamas a la clase de Primavera cuando la tengas lista
                        // PrimaveraScreen(navController)
                        Text("Pantalla de Primavera", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
                    }

                    composable(Screen.Verano.route) {
                        // VeranoScreen(navController)
                        Text("Pantalla de Verano", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
                    }

                    composable(Screen.Otono.route) {
                        // OtonoScreen(navController)
                        Text("Pantalla de Otoño", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
                    }

                    composable(Screen.Invierno.route) {
                        // InviernoScreen(navController)
                        Text("Pantalla de Invierno", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
                    }

                    // 5. MERCAJOJA
                    composable(Screen.Mercajoja.route) {
                        // MercajojaScreen(navController)
                    }
                }
            }
        }
    }
}