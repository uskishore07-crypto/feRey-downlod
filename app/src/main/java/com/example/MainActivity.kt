package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Moment
import com.example.ui.AppNavDestination
import com.example.ui.BottomTab
import com.example.ui.MainViewModel
import com.example.ui.components.FeReyStartupAnimation
import com.example.ui.screens.AccountSetupScreen
import com.example.ui.screens.AddMomentScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReplayScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TimelineScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.FeReyTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val systemIsDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> systemIsDark
            }

            FeReyTheme(darkTheme = isDarkTheme) {
                FeReyApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FeReyApp(viewModel: MainViewModel) {
    val appDestination by viewModel.appDestination.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isAddMomentOpen by viewModel.isAddMomentOpen.collectAsStateWithLifecycle()
    val isReplayActive by viewModel.isReplayActive.collectAsStateWithLifecycle()

    val todayMoments by viewModel.todayMoments.collectAsStateWithLifecycle()
    val allMoments by viewModel.allMoments.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    val replayDate by viewModel.replayDate.collectAsStateWithLifecycle()
    val replayMoments by viewModel.replayMoments.collectAsStateWithLifecycle()
    val replayCurrentIndex by viewModel.replayCurrentIndex.collectAsStateWithLifecycle()
    val isReplayPlaying by viewModel.isReplayPlaying.collectAsStateWithLifecycle()

    Crossfade(targetState = appDestination, label = "app_destination_crossfade") { destination ->
        when (destination) {
            AppNavDestination.STARTUP_ANIMATION -> {
                FeReyStartupAnimation(
                    onAnimationFinished = { viewModel.onStartupAnimationComplete() }
                )
            }

            AppNavDestination.WELCOME -> {
                WelcomeScreen(
                    onGetStarted = { viewModel.onGetStartedClicked() }
                )
            }

            AppNavDestination.ACCOUNT_SETUP -> {
                AccountSetupScreen(
                    onAccountCreated = { name -> viewModel.completeAccountSetup(name) },
                    onContinueAsGuest = { viewModel.continueAsGuest() }
                )
            }

            AppNavDestination.MAIN_APP -> {
                if (isReplayActive) {
                    ReplayScreen(
                        dateString = replayDate,
                        moments = replayMoments,
                        currentIndex = replayCurrentIndex,
                        isPlaying = isReplayPlaying,
                        onNext = { viewModel.nextReplayMoment() },
                        onPrev = { viewModel.prevReplayMoment() },
                        onTogglePlay = { viewModel.toggleReplayPlayPause() },
                        onClose = { viewModel.stopReplay() },
                        onRestart = { viewModel.setReplayIndex(0) }
                    )
                } else if (isAddMomentOpen) {
                    AddMomentScreen(
                        onDismiss = { viewModel.closeAddMoment() },
                        onSaveMoment = { photoUri, thought, song, artist ->
                            viewModel.saveMoment(photoUri, thought, song, artist)
                        }
                    )
                } else {
                    Scaffold(
                        bottomBar = {
                            FeReyBottomNavigation(
                                selectedTab = selectedTab,
                                onTabSelected = { tab ->
                                    if (tab == BottomTab.REPLAY) {
                                        // Launch replay for today if exists, or all moments
                                        val momentsToReplay = if (todayMoments.isNotEmpty()) todayMoments else allMoments
                                        val replayDateStr = if (todayMoments.isNotEmpty()) viewModel.todayDateString else (allMoments.firstOrNull()?.dateString ?: viewModel.todayDateString)
                                        viewModel.startReplayForDate(replayDateStr, momentsToReplay)
                                    } else {
                                        viewModel.selectTab(tab)
                                    }
                                }
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.background,
                        contentWindowInsets = WindowInsets.navigationBars,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("main_app_scaffold")
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (selectedTab) {
                                BottomTab.HOME -> {
                                    HomeScreen(
                                        greeting = viewModel.getGreetingMessage(),
                                        todayMoments = todayMoments,
                                        onAddMoment = { viewModel.openAddMoment() },
                                        onReplayToday = {
                                            viewModel.startReplayForDate(
                                                viewModel.todayDateString,
                                                todayMoments
                                            )
                                        },
                                        onDeleteMoment = { moment -> viewModel.deleteMoment(moment) }
                                    )
                                }

                                BottomTab.TIMELINE -> {
                                    TimelineScreen(
                                        allMoments = allMoments,
                                        onReplayDate = { dateStr, moments ->
                                            viewModel.startReplayForDate(dateStr, moments)
                                        },
                                        onDeleteMoment = { moment -> viewModel.deleteMoment(moment) }
                                    )
                                }

                                BottomTab.REPLAY -> {
                                    // Replay tab displays replay or timeline launcher
                                    TimelineScreen(
                                        allMoments = allMoments,
                                        onReplayDate = { dateStr, moments ->
                                            viewModel.startReplayForDate(dateStr, moments)
                                        },
                                        onDeleteMoment = { moment -> viewModel.deleteMoment(moment) }
                                    )
                                }

                                BottomTab.SETTINGS -> {
                                    SettingsScreen(
                                        currentThemeMode = themeMode,
                                        username = username,
                                        totalMoments = allMoments.size,
                                        onSetThemeMode = { mode -> viewModel.setThemeMode(mode) },
                                        onClearAllData = { viewModel.clearAllData() },
                                        onLogOut = { viewModel.logOut() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeReyBottomNavigation(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp,
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )
            .height(68.dp)
            .testTag("ferey_bottom_nav")
    ) {
        // 1. Home
        NavigationBarItem(
            selected = selectedTab == BottomTab.HOME,
            onClick = { onTabSelected(BottomTab.HOME) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == BottomTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = stringResource(R.string.nav_home),
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_home),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onBackground,
                selectedTextColor = MaterialTheme.colorScheme.onBackground,
                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_home")
        )

        // 2. Timeline
        NavigationBarItem(
            selected = selectedTab == BottomTab.TIMELINE,
            onClick = { onTabSelected(BottomTab.TIMELINE) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == BottomTab.TIMELINE) Icons.Filled.History else Icons.Outlined.History,
                    contentDescription = stringResource(R.string.nav_timeline),
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_timeline),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onBackground,
                selectedTextColor = MaterialTheme.colorScheme.onBackground,
                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_timeline")
        )

        // 3. Replay
        NavigationBarItem(
            selected = selectedTab == BottomTab.REPLAY,
            onClick = { onTabSelected(BottomTab.REPLAY) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == BottomTab.REPLAY) Icons.Filled.PlayCircleOutline else Icons.Outlined.PlayCircleOutline,
                    contentDescription = stringResource(R.string.nav_replay),
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_replay),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onBackground,
                selectedTextColor = MaterialTheme.colorScheme.onBackground,
                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_replay")
        )

        // 4. Settings
        NavigationBarItem(
            selected = selectedTab == BottomTab.SETTINGS,
            onClick = { onTabSelected(BottomTab.SETTINGS) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == BottomTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.nav_settings),
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_settings),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onBackground,
                selectedTextColor = MaterialTheme.colorScheme.onBackground,
                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_settings")
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
