package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AdventureViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AdventureViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: AdventureViewModel) {
    val currentRoute by viewModel.currentRoute.collectAsState()

    // Determine bottom navigation visibility
    val showBottomBar = currentRoute in listOf("home", "discovery", "favorites", "settings")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("app_bottom_bar")
                ) {
                    // Home Tab Item
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = { viewModel.navigateTo("home") },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == "home") Icons.Default.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home") },
                        modifier = Modifier.testTag("nav_home")
                    )

                    // Discovery Deck Tab Item
                    NavigationBarItem(
                        selected = currentRoute == "discovery",
                        onClick = { viewModel.navigateTo("discovery") },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == "discovery") Icons.Default.Casino else Icons.Outlined.Casino,
                                contentDescription = "Discovery"
                            )
                        },
                        label = { Text("Discover") },
                        modifier = Modifier.testTag("nav_discovery")
                    )

                    // Favorites Log Tab Item
                    NavigationBarItem(
                        selected = currentRoute == "favorites",
                        onClick = { viewModel.navigateTo("favorites") },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == "favorites") Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorites"
                            )
                        },
                        label = { Text("Saved") },
                        modifier = Modifier.testTag("nav_favorites")
                    )

                    // Settings Settings Tab Item
                    NavigationBarItem(
                        selected = currentRoute == "settings",
                        onClick = { viewModel.navigateTo("settings") },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == "settings") Icons.Default.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings") },
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Smooth routing transition crossfade
            Crossfade(
                targetState = currentRoute,
                label = "NavigationTransition"
            ) { route ->
                when (route) {
                    "onboarding" -> OnboardingScreen(viewModel = viewModel)
                    "home" -> HomeScreen(viewModel = viewModel)
                    "discovery" -> DiscoveryScreen(viewModel = viewModel)
                    "favorites" -> FavoritesScreen(viewModel = viewModel)
                    "settings" -> SettingsScreen(viewModel = viewModel)
                    "place_details" -> PlaceDetailsScreen(viewModel = viewModel)
                    else -> OnboardingScreen(viewModel = viewModel) // Entry fallback
                }
            }
        }
    }
}
