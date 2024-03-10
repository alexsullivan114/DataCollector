package com.alexsullivan.datacollor.routing

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alexsullivan.datacollor.home.HomeScreen
import com.alexsullivan.datacollor.insights.InsightsScreen
import com.alexsullivan.datacollor.routing.Screen.Home
import com.alexsullivan.datacollor.routing.Screen.Insights
import com.alexsullivan.datacollor.routing.Screen.Insights.navigate
import com.alexsullivan.datacollor.routing.Screen.Settings
import com.alexsullivan.datacollor.settings.SettingsScreen

@Composable
fun Router() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "Home") {
        composable(Home.screenName) {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(Settings) },
                onNavigateToInsights = { trackableId -> navController.navigate(trackableId = trackableId) }
            )
        }
        composable(Settings.screenName) { SettingsScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(
            Insights.screenName,
            arguments = Insights.navArguments
        ) { InsightsScreen(onNavigateBack = { navController.popBackStack() }) }
    }
}
