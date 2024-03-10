package com.alexsullivan.datacollor.routing

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alexsullivan.datacollor.R
import com.alexsullivan.datacollor.analysis.AnalysisScreen
import com.alexsullivan.datacollor.home.HomeScreen
import com.alexsullivan.datacollor.insights.InsightsScreen
import com.alexsullivan.datacollor.previousdays.PreviousDaysScreen
import com.alexsullivan.datacollor.routing.Screen.Analysis
import com.alexsullivan.datacollor.routing.Screen.Home
import com.alexsullivan.datacollor.routing.Screen.Insights
import com.alexsullivan.datacollor.routing.Screen.Insights.navigate
import com.alexsullivan.datacollor.routing.Screen.PreviousDays
import com.alexsullivan.datacollor.routing.Screen.Settings
import com.alexsullivan.datacollor.settings.SettingsScreen

private sealed class NavItem(val route: String, @StringRes val resourceId: Int) {
    @Composable
    abstract fun Icon()
    object Tracking : NavItem(Home.screenName, R.string.tracking) {
        @Composable
        override fun Icon() {
            Icon(painter = painterResource(id = R.drawable.monitoring), null)
        }

    }
    object Insights : NavItem(Analysis.screenName, R.string.insights) {
        @Composable
        override fun Icon() {
            Icon(painter = painterResource(id = R.drawable.science), null)
        }
    }
}

@Composable
fun Router() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            NavigationBar {
                listOf(NavItem.Tracking, NavItem.Insights).forEach { screen ->
                    NavigationBarItem(
                        icon = { screen.Icon() },
                        label = { Text(stringResource(screen.resourceId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.screenName,
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            composable(Home.screenName) {
                HomeScreen(
                    onNavigateToSettings = { navController.navigate(Settings) },
                    onNavigateToInsights = { trackableId -> navController.navigate(trackableId = trackableId) },
                    onNavigateToPreviousDays = { navController.navigate(PreviousDays) }
                )
            }
            composable(Settings.screenName) { SettingsScreen(onNavigateBack = { navController.popBackStack() }) }
            composable(
                Insights.screenName,
                arguments = Insights.navArguments
            ) { InsightsScreen(onNavigateBack = { navController.popBackStack() }) }
            composable(PreviousDays.screenName) { PreviousDaysScreen() }
            composable(Analysis.screenName) { AnalysisScreen() }
        }
    }
}
