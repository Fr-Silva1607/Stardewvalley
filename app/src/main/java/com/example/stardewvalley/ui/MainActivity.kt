package com.example.stardewvalley.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            
            val cultiVM: CultivoViewModel = viewModel()
            val checkListVM: CheckListViewModel = viewModel()
            
            val farmId = remember { prefs.getInt("selected_farm_id", -1) }

            LaunchedEffect(farmId) {
                if (farmId != -1) {
                    cultiVM.initFarm(farmId)
                }
            }

            GestionarTiempoJuego(context = context, cultiVM = cultiVM)

            // Obtenemos la ruta. Si no existe o no es válida, FORZAMOS elecciones.
            val bandoDeEstaGranja = remember(farmId) {
                if (farmId != -1) prefs.getString("ruta_$farmId", null) else null
            }

            val destinoInicial = when (bandoDeEstaGranja) {
                "mercajoja" -> "joja_screen"
                "centro_civico" -> "calendario"
                else -> "elecciones" 
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                NavHost(
                    navController = navController, 
                    startDestination = destinoInicial,
                    enterTransition = { fadeIn(animationSpec = snap()) },
                    exitTransition = { fadeOut(animationSpec = snap()) },
                    popEnterTransition = { fadeIn(animationSpec = snap()) },
                    popExitTransition = { fadeOut(animationSpec = snap()) }
                ) {
                    composable("elecciones") {
                        EleccionGranjaScreen(navController) {
                            // Al elegir, navegamos según el bando que el usuario acaba de guardar
                            val bandoElegido = prefs.getString("ruta_$farmId", null)
                            val destino = if (bandoElegido == "mercajoja") "joja_screen" else "calendario"
                            
                            navController.navigate(destino) {
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

                    composable("joja_screen") {
                        MercaJojaScreen(navController)
                    }
                }
            }
        }
    }
}
