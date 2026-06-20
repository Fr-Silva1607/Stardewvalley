package com.example.stardewvalley.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.stardewvalley.util.NotificationHelper
import com.example.stardewvalley.viewmodel.CultivoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CentroCivico : Screen("centro_civico")
    object Mercajoja : Screen("mercajoja")
    object Temporadas : Screen("temporadas")
    object Primavera : Screen("primavera")
    object Verano : Screen("verano")
    object Otono : Screen("otono")
    object Invierno : Screen("invierno")

    companion object {
        var yaVioTemporadas: Boolean = false
    }
}

/**
 * Función que simula el tiempo de Stardew Valley:
 * - 1 hora de juego = 42 segundos reales.
 * - 1 día (6:00 AM a 2:00 AM = 20 horas) = 14 minutos reales.
 * - En Caverna Calavera el tiempo se alarga a 18 minutos (aprox 54s por hora).
 */
@Composable
fun GestionarTiempoJuego(
    context: Context,
    cultiVM: CultivoViewModel,
    enCaverna: Boolean = false
) {
    val notificationHelper = NotificationHelper(context)

    LaunchedEffect(enCaverna) {
        // 14 min = 840s. 840s / 20h = 42s por hora.
        // 18 min = 1080s. 1080s / 20h = 54s por hora.
        val segundosPorHora = if (enCaverna) 54L else 42L
        
        while (isActive) {
            // Simular las 20 horas del día
            for (hora in 6..25) { 
                delay(segundosPorHora * 1000L)
            }

            // Al terminar las 20 horas, avanzamos el día en la app
            val diaSiguiente = cultiVM.diaActual.value + 1
            cultiVM.setDiaActual(diaSiguiente)

            // Comprobar si hay cultivos listos
            val listos = cultiVM.cultivosPlantados.value.filter { 
                diaSiguiente - it.diaPlante >= it.diasCrecimiento 
            }

            val horaReal = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            if (listos.isNotEmpty()) {
                notificationHelper.sendNotification(
                    "¡Cultivo listo!",
                    "Día $diaSiguiente ($horaReal): Tus ${listos.first().nombre} están listos para cosechar."
                )
            } else {
                notificationHelper.sendNotification(
                    "Amanecer en la granja",
                    "Es el día $diaSiguiente. Hora real: $horaReal. ¡Que tengas un buen día!"
                )
            }
        }
    }
}
