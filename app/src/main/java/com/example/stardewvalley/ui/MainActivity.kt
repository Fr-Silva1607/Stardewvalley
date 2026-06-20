package com.example.stardewvalley.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stardewvalley.ui.screen.EleccionGranjaScreen
import com.example.stardewvalley.ui.screen.CalendarioScreen
import com.example.stardewvalley.ui.screen.CheckListScreen
import com.example.stardewvalley.ui.screen.MercaJojaScreen
import com.example.stardewvalley.navigation.GestionarTiempoJuego
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stardewvalley.viewmodel.CultivoViewModel
import com.example.stardewvalley.viewmodel.CheckListViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("StardewPrefs", MODE_PRIVATE) }
            
            // ViewModels compartidos
            val cultiVM: CultivoViewModel = viewModel()
            val checkListVM: CheckListViewModel = viewModel()

            val farmId = remember { prefs.getInt("selected_farm_id", -1) }

            // Inicializar el ViewModel con la granja seleccionada
            LaunchedEffect(farmId) {
                if (farmId != -1) {
                    cultiVM.initFarm(farmId)
                }
            }

            // Iniciamos la simulación del tiempo globalmente
            GestionarTiempoJuego(context = context, cultiVM = cultiVM)

            val bandoDeEstaGranja = remember(farmId) {
                if (farmId != -1) prefs.getString("ruta_$farmId", null) else null
            }

            val destinoInicial = if (bandoDeEstaGranja == null) "elecciones" else "calendario"

            NavHost(navController = navController, startDestination = destinoInicial) {
                composable("elecciones") {
                    EleccionGranjaScreen(navController) {
                        navController.navigate("calendario") {
                            popUpTo("elecciones") { inclusive = true }
                        }
                    }
                }

                composable("joja_screen") {
                    MercaJojaScreen(navController) {
                        navController.navigate("calendario") {
                            popUpTo("elecciones") { inclusive = true }
                        }
                    }
                }

                composable("calendario") {
                    CalendarioScreen(navController, cultiVM = cultiVM)
                }

                composable("centro_civico") {
                    CheckListScreen(navController = navController, viewModel = checkListVM)
                }
            }
        }
    }
}
