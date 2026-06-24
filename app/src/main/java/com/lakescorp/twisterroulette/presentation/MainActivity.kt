package com.lakescorp.twisterroulette.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.lakescorp.twisterroulette.R
import com.lakescorp.twisterroulette.presentation.main.MainScreen
import com.lakescorp.twisterroulette.presentation.main.MainViewModel
import com.lakescorp.twisterroulette.presentation.modes.ModesScreen
import com.lakescorp.twisterroulette.presentation.modes.ModesViewModel
import com.lakescorp.twisterroulette.presentation.settings.SettingsScreen
import com.lakescorp.twisterroulette.presentation.settings.SettingsViewModel
import com.lakescorp.twisterroulette.presentation.theme.TwisterRouletteTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel by viewModels()
            val settings by mainViewModel.settings.collectAsState()

            TwisterRouletteTheme(theme = settings.theme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RootScreen(mainViewModel = mainViewModel)
                }
            }
        }
    }
}

private const val TAB_PLAY = 0
private const val TAB_MODES = 1
private const val TAB_SETTINGS = 2

@Composable
fun RootScreen(mainViewModel: MainViewModel) {
    // Bottom-nav selection is a simple tab switch — both panes share this Scaffold,
    // so selecting "Settings" swaps the content in place rather than pushing a screen.
    var selectedTab by rememberSaveable { mutableStateOf(TAB_PLAY) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = selectedTab == TAB_PLAY,
                    onClick = { selectedTab = TAB_PLAY },
                    icon = { Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_play), contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_play)) }
                )
                NavigationBarItem(
                    selected = selectedTab == TAB_MODES,
                    onClick = { selectedTab = TAB_MODES },
                    icon = { Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_dices), contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_modes)) }
                )
                NavigationBarItem(
                    selected = selectedTab == TAB_SETTINGS,
                    onClick = { selectedTab = TAB_SETTINGS },
                    icon = { Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_settings_2), contentDescription = null) },
                    label = { Text(stringResource(R.string.settings)) }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                TAB_MODES -> {
                    val modesViewModel = hiltViewModel<ModesViewModel>()
                    ModesScreen(viewModel = modesViewModel)
                }
                TAB_SETTINGS -> {
                    val settingsViewModel = hiltViewModel<SettingsViewModel>()
                    SettingsScreen(viewModel = settingsViewModel)
                }
                else -> {
                    MainScreen(viewModel = mainViewModel)
                }
            }
        }
    }
}
