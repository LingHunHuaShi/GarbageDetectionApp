package com.zzh.garbagedetection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zzh.garbagedetection.ui.DetectPageContainer
import com.zzh.garbagedetection.ui.SettingsPageContainer
import com.zzh.garbagedetection.ui.components.BottomNavItem
import com.zzh.garbagedetection.ui.components.navItems
import com.zzh.garbagedetection.ui.theme.GarbageDetectionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GarbageDetectionTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                            navItems.forEach { item ->
                                NavigationBarItem(
                                    icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                                    label = { Text(text = item.label) },
                                    selected = currentRoute == item.route,
                                    onClick = {
                                        if (currentRoute != item.route) {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = BottomNavItem.Detect.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(BottomNavItem.Detect.route) { DetectPageContainer(modifier = Modifier.fillMaxSize()) }
                        composable(BottomNavItem.Settings.route) { SettingsPageContainer(modifier = Modifier.fillMaxSize()) }
                    }
                }
            }
        }
    }
}