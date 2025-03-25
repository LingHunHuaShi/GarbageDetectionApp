package com.zzh.garbagedetection.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Detect : BottomNavItem("detect", Icons.Default.Search, "检测")
    object Settings : BottomNavItem("settings", Icons.Default.Settings, "设置")
}

val navItems = listOf(
    BottomNavItem.Detect,
    BottomNavItem.Settings
)