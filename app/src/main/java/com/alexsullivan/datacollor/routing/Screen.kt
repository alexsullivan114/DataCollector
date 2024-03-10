package com.alexsullivan.datacollor.routing

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen {

    abstract val screenName: String
    open val navArguments: List<NamedNavArgument> = emptyList()

    object Home : Screen() {
        override val screenName = "home"
    }

    object Settings : Screen() {
        override val screenName = "settings"
    }

    object Insights : Screen() {
        private const val screenNameRoot = "insights/"
        const val trackableIdKey = "trackableId"
        override val screenName = "$screenNameRoot{$trackableIdKey}"
        override val navArguments = listOf(navArgument(trackableIdKey) { NavType.StringType })
        fun NavController.navigate(trackableId: String) = navigate("$screenNameRoot$trackableId")
    }
}

internal fun NavController.navigate(screen: Screen) = navigate(screen.screenName)
