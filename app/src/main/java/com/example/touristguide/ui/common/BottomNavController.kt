package com.example.touristguide.ui.common

import android.view.View
import com.example.touristguide.R
import androidx.navigation.NavController

object BottomNavController {
    private val topLevelDestinations = setOf(
        R.id.homeFragment,
        R.id.placesListFragment,
        R.id.feedFragment,
        R.id.favoritesFragment,
        R.id.profileFragment
    )

    fun bind(root: View, navController: NavController) {
        root.findViewById<View>(R.id.bottomNavigation) ?: return
        bindItem(root, navController, R.id.navHome, R.id.homeFragment)
        bindItem(root, navController, R.id.navPlaces, R.id.placesListFragment)
        bindItem(root, navController, R.id.navFeed, R.id.feedFragment)
        bindItem(root, navController, R.id.navFavorites, R.id.favoritesFragment)
        bindItem(root, navController, R.id.navProfile, R.id.profileFragment)
    }

    private fun bindItem(root: View, navController: NavController, viewId: Int, destinationId: Int) {
        val item = root.findViewById<View>(viewId) ?: return
        val isSelected = navController.currentDestination?.id == destinationId
        item.alpha = if (isSelected || destinationId !in topLevelDestinations) 1f else 0.82f
        item.setOnClickListener {
            if (navController.currentDestination?.id != destinationId) {
                navController.navigate(destinationId)
            }
        }
    }
}
