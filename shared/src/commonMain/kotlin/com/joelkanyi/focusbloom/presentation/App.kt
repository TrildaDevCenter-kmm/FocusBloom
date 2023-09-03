package com.joelkanyi.focusbloom.presentation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabDisposable
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.joelkanyi.focusbloom.presentation.calendar.CalendarScreen
import com.joelkanyi.focusbloom.presentation.home.HomeScreen
import com.joelkanyi.focusbloom.presentation.settings.SettingsScreen
import com.joelkanyi.focusbloom.presentation.statistics.StatisticsScreen
import com.joelkanyi.focusbloom.presentation.theme.FocusBloomTheme

@OptIn(ExperimentalVoyagerApi::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun App() {
    FocusBloomTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            /**
             * How to know if the app is running on desktop or mobile?
             * use windowManager.defaultDisplay.rotation
             */
            val windowSizeClass = calculateWindowSizeClass()
            val useNavRail = windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact
            TabNavigator(
                HomeTab,
                tabDisposable = {
                    TabDisposable(
                        navigator = it,
                        tabs = listOf(HomeTab, StatisticsTab, SettingsTab, CalendarTab),
                    )
                },
            ) {
                if (useNavRail) {
                    Row {
                        NavigationRailBar(
                            tabNavigator = it,
                            navRailItems = listOf(HomeTab, StatisticsTab, SettingsTab, CalendarTab),
                        )

                        CurrentScreen()
                    }
                } else {
                    Scaffold(
                        content = {
                            CurrentTab()
                        },
                        floatingActionButtonPosition = FabPosition.Center,
                        floatingActionButton = {
                            FloatingActionButton(
                                modifier = Modifier
                                    .offset(y = 60.dp)
                                    .size(42.dp),
                                containerColor = MaterialTheme.colorScheme.primary,
                                onClick = {
                                    // navController.navigate(AddTaskScreenDestination.route)
                                },
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
                                shape = CircleShape,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        },
                        bottomBar = {
                            BottomNavigation(
                                backgroundColor = MaterialTheme.colorScheme.background,
                            ) {
                                TabNavigationItem(HomeTab)
                                TabNavigationItem(CalendarTab)
                                TabNavigationItem(StatisticsTab)
                                TabNavigationItem(SettingsTab)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current

    BottomNavigationItem(
        modifier = Modifier.offset(
            x = when (tab.options.index) {
                (0u).toUShort() -> 0.dp
                (1u).toUShort() -> (-24).dp
                (2u).toUShort() -> 24.dp
                (3u).toUShort() -> 0.dp
                else -> 0.dp
            },
        ),
        selected = tabNavigator.current == tab,
        onClick = { tabNavigator.current = tab },
        icon = {
            Icon(
                painter = tab.options.icon!!,
                contentDescription = tab.options.title,
            )
        },
    )
}

internal object HomeTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Home"
            val icon = rememberVectorPainter(Icons.Default.Home)

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon,
                )
            }
        }

    @Composable
    override fun Content() {
        HomeScreen()
    }
}

internal object CalendarTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Calendar"
            val icon = rememberVectorPainter(Icons.Default.Call)

            return remember {
                TabOptions(
                    index = 1u,
                    title = title,
                    icon = icon,
                )
            }
        }

    @Composable
    override fun Content() {
        CalendarScreen()
    }
}

internal object StatisticsTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Statistics"
            val icon = rememberVectorPainter(Icons.Default.Star)

            return remember {
                TabOptions(
                    index = 2u,
                    title = title,
                    icon = icon,
                )
            }
        }

    @Composable
    override fun Content() {
        StatisticsScreen()
    }
}

internal object SettingsTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Settings"
            val icon = rememberVectorPainter(Icons.Default.Search)

            return remember {
                TabOptions(
                    index = 3u,
                    title = title,
                    icon = icon,
                )
            }
        }

    @Composable
    override fun Content() {
        SettingsScreen()
    }
}

@Composable
fun NavigationRailBar(
    modifier: Modifier = Modifier,
    tabNavigator: TabNavigator,
    navRailItems: List<Tab>,
) {
    NavigationRail(
        modifier = modifier.fillMaxHeight().alpha(0.95F),
        containerColor = MaterialTheme.colorScheme.surface,
        header = {
            Icon(
                modifier = Modifier.size(42.dp),
                imageVector = Icons.Default.AccountBox,
                // painter = painterResource("n_logo.png"),
                contentDescription = "Logo",
            )
        },
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        navRailItems.forEach { item ->

            NavigationRailItem(
                icon = {
                    item.options.icon?.let {
                        Icon(
                            painter = it,
                            contentDescription = item.options.title,
                        )
                    }
                },
                label = { Text(text = item.options.title) },
                // selectedContentColor = PrimaryColor,
                // unselectedContentColor = Gray,
                alwaysShowLabel = false,
                selected = tabNavigator.current == item,
                onClick = { tabNavigator.current = item },
            )
        }
    }
}