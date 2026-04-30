package com.example.stardewvalley.navigation

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