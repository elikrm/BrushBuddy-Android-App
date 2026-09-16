package com.elnaz.brushbuddy.utils
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.elnaz.brushbuddy.models.BottomNavItem

object Constants {
    val BottomNavItems = listOf(
        // Home screen
        BottomNavItem(
            label = "Home",
            icon = Icons.Filled.Home,
            route = "home"
        ),
        // Search screen
        BottomNavItem(
            label = "History",
            icon = Icons.Filled.History,
            route = "history"
        ),
        // Profile screen
        BottomNavItem(
            label = "Profile",
            icon = Icons.Filled.Person,
            route = "profile"
        )
    )
}