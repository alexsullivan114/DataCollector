package com.alexsullivan.datacollor.routing

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen {

    abstract val screenName: String
    open val navArguments: List<NamedNavArgument> = emptyList()

    data object Home : Screen() {
        override val screenName = "home"
    }

    data object Settings : Screen() {
        override val screenName = "settings"
    }

    data object Insights : Screen() {
        private const val screenNameRoot = "insights/"
        const val trackableIdKey = "trackableId"
        override val screenName = "$screenNameRoot{$trackableIdKey}"
        override val navArguments = listOf(navArgument(trackableIdKey) { NavType.StringType })
        fun NavController.navigateToInsights(trackableId: String) = navigate("$screenNameRoot$trackableId")
    }

    data object PreviousDays: Screen() {
        override val screenName = "previousdays"
    }

    data object Analysis: Screen() {
        override val screenName = "analysis"

    }
}

internal fun NavController.navigateToScreen(screen: Screen) = navigate(screen.screenName)
