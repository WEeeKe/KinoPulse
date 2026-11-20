package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.ui.components.NotificationsBottomSheet
import com.example.ui.navigation.ChatRoute
import com.example.ui.navigation.FavoritesRoute
import com.example.ui.navigation.HomeRoute
import com.example.ui.navigation.MovieDetailRoute
import com.example.ui.navigation.PlayerRoute
import com.example.ui.navigation.SearchRoute
import com.example.ui.navigation.SettingsRoute
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MovieDetailScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ThemeMode

private data class TopNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: Any,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KinoPulseAppRoot(viewModel: KinoPulseViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var favoritesInitialTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.consumeSnackbar()
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isFullScreenRoute = currentDestination?.hasRoute<MovieDetailRoute>() == true ||
        currentDestination?.hasRoute<PlayerRoute>() == true

    val topNavItems = remember {
        listOf(
            TopNavItem("Главная", Icons.Filled.Home, Icons.Outlined.Home, HomeRoute, "nav_home"),
            TopNavItem("Поиск", Icons.Filled.Search, Icons.Outlined.Search, SearchRoute, "nav_search"),
            TopNavItem("Избранное", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder, FavoritesRoute, "nav_favorites"),
            TopNavItem("Киноклуб", Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat, ChatRoute("global"), "nav_chat"),
            TopNavItem("Профиль", Icons.Filled.Person, Icons.Outlined.Person, SettingsRoute, "nav_settings")
        )
    }

    fun navigateTopLevel(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = if (isFullScreenRoute) WindowInsets(0) else WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                if (!isFullScreenRoute) {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "KinoPulse",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (uiState.effectiveOfflineMode) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.CloudOff,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = "Офлайн",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        actions = {
                            val isNight = uiState.preferences.themeMode == ThemeMode.NIGHT
                            IconButton(
                                onClick = { viewModel.toggleNightMode() },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("topbar_night_toggle")
                            ) {
                                Icon(
                                    imageVector = if (isNight) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                    contentDescription = "Переключить ночной режим"
                                )
                            }
                            IconButton(
                                onClick = { showNotificationsSheet = true },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("topbar_notifications_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (uiState.unreadNotificationsCount > 0) {
                                            Badge {
                                                Text(uiState.unreadNotificationsCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Notifications,
                                        contentDescription = "Уведомления о новинках"
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            },
            bottomBar = {
                if (!isFullScreenRoute && !useNavRail) {
                    NavigationBar {
                        topNavItems.forEach { item ->
                            val selected = isRouteSelected(currentDestination, item.route)
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (item.route == FavoritesRoute) favoritesInitialTab = 0
                                    navigateTopLevel(item.route)
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label, maxLines = 1) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (!isFullScreenRoute && useNavRail) {
                    NavigationRail {
                        Spacer(modifier = Modifier.weight(0.1f))
                        topNavItems.forEach { item ->
                            val selected = isRouteSelected(currentDestination, item.route)
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    if (item.route == FavoritesRoute) favoritesInitialTab = 0
                                    navigateTopLevel(item.route)
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = HomeRoute
                    ) {
                        composable<HomeRoute> {
                            HomeScreen(
                                uiState = uiState,
                                onMovieClick = { id -> navController.navigate(MovieDetailRoute(id)) },
                                onPlayClick = { id -> navController.navigate(PlayerRoute(id)) },
                                onToggleFavorite = viewModel::toggleFavorite,
                                onGenreClick = { genre ->
                                    viewModel.selectSingleGenreAndReset(genre)
                                    navigateTopLevel(SearchRoute)
                                },
                                onOpenOfflineTab = {
                                    favoritesInitialTab = 1
                                    navigateTopLevel(FavoritesRoute)
                                },
                                onOpenSearch = { navigateTopLevel(SearchRoute) }
                            )
                        }

                        composable<SearchRoute> {
                            SearchScreen(
                                uiState = uiState,
                                onQueryChange = viewModel::updateSearchQuery,
                                onToggleGenre = viewModel::toggleGenreFilter,
                                onSelectYearPreset = viewModel::selectYearPreset,
                                onCustomYearRange = viewModel::updateCustomYearRange,
                                onSelectSort = viewModel::updateSortOption,
                                onToggleOfflineOnly = viewModel::toggleOfflineOnlyFilter,
                                onResetFilters = viewModel::resetSearchFilters,
                                onMovieClick = { id -> navController.navigate(MovieDetailRoute(id)) },
                                onToggleFavorite = viewModel::toggleFavorite
                            )
                        }

                        composable<FavoritesRoute> {
                            FavoritesScreen(
                                uiState = uiState,
                                initialTab = favoritesInitialTab,
                                onMovieClick = { id -> navController.navigate(MovieDetailRoute(id)) },
                                onPlayClick = { id -> navController.navigate(PlayerRoute(id)) },
                                onToggleFavorite = viewModel::toggleFavorite,
                                onDownloadMovie = viewModel::downloadMovie,
                                onRemoveDownload = viewModel::removeDownload
                            )
                        }

                        composable<ChatRoute> { backStackEntry ->
                            val initialRoom = backStackEntry.toRoute<ChatRoute>().movieId
                            var activeRoomId by remember(initialRoom) { mutableStateOf(initialRoom) }
                            val messages by viewModel.observeChat(activeRoomId)
                                .collectAsStateWithLifecycle(initialValue = emptyList())

                            ChatScreen(
                                selectedRoomId = activeRoomId,
                                movies = uiState.allMovies,
                                messages = messages,
                                onSelectRoom = { activeRoomId = it },
                                onSendMessage = { text, replyTo ->
                                    viewModel.sendChatMessage(activeRoomId, text, replyTo)
                                },
                                onToggleLike = viewModel::toggleChatLike
                            )
                        }

                        composable<SettingsRoute> {
                            SettingsScreen(
                                uiState = uiState,
                                onSetThemeMode = viewModel::setThemeMode,
                                onSetAmoledNight = viewModel::setAmoledNightMode,
                                onSetOfflineMode = viewModel::setOfflineMode,
                                onSetNotificationsEnabled = viewModel::setNotificationsEnabled,
                                onTriggerNotification = viewModel::triggerNewReleaseNotification,
                                onClearVolatileCache = viewModel::clearVolatileCache,
                                onSaveDisplayName = viewModel::setUserDisplayName,
                                onExportSyncCode = viewModel::exportSyncCode,
                                onImportSyncCode = viewModel::importSyncCode
                            )
                        }

                        composable<MovieDetailRoute> { backStackEntry ->
                            val movieId = backStackEntry.toRoute<MovieDetailRoute>().movieId
                            val movie by viewModel.observeMovie(movieId)
                                .collectAsStateWithLifecycle(initialValue = null)
                            val currentMovie = movie ?: uiState.allMovies.firstOrNull { it.id == movieId }
                            if (currentMovie != null) {
                                MovieDetailScreen(
                                    movie = currentMovie,
                                    relatedMovies = viewModel.relatedMovies(currentMovie, uiState.allMovies),
                                    downloadProgress = uiState.downloadProgress[currentMovie.id],
                                    onBack = { navController.popBackStack() },
                                    onPlay = { navController.navigate(PlayerRoute(currentMovie.id)) },
                                    onToggleFavorite = { viewModel.toggleFavorite(currentMovie.id) },
                                    onSelectCollection = { col -> viewModel.setFavoriteCollection(currentMovie.id, col) },
                                    onDownload = { viewModel.downloadMovie(currentMovie) },
                                    onRemoveDownload = { viewModel.removeDownload(currentMovie) },
                                    onOpenMovieChat = {
                                        navController.navigate(ChatRoute(currentMovie.id))
                                    },
                                    onOpenRelatedMovie = { relatedId ->
                                        navController.navigate(MovieDetailRoute(relatedId))
                                    }
                                )
                            }
                        }

                        composable<PlayerRoute> { backStackEntry ->
                            val movieId = backStackEntry.toRoute<PlayerRoute>().movieId
                            val movie by viewModel.observeMovie(movieId)
                                .collectAsStateWithLifecycle(initialValue = null)
                            val currentMovie = movie ?: uiState.allMovies.firstOrNull { it.id == movieId }
                            if (currentMovie != null) {
                                PlayerScreen(
                                    movie = currentMovie,
                                    isOfflineMode = uiState.effectiveOfflineMode,
                                    onSaveProgress = { seconds ->
                                        viewModel.updateWatchProgress(currentMovie.id, seconds)
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNotificationsSheet) {
        NotificationsBottomSheet(
            notifications = uiState.notifications,
            onDismiss = { showNotificationsSheet = false },
            onNotificationClick = { item ->
                viewModel.markNotificationRead(item.id)
                showNotificationsSheet = false
                item.movieId?.let { id ->
                    navController.navigate(MovieDetailRoute(id))
                }
            },
            onMarkAllRead = viewModel::markAllNotificationsRead,
            onCheckNewReleases = viewModel::triggerNewReleaseNotification
        )
    }
}

private fun isRouteSelected(
    destination: androidx.navigation.NavDestination?,
    route: Any
): Boolean {
    if (destination == null) return false
    return when (route) {
        is HomeRoute -> destination.hasRoute<HomeRoute>()
        is SearchRoute -> destination.hasRoute<SearchRoute>()
        is FavoritesRoute -> destination.hasRoute<FavoritesRoute>()
        is ChatRoute -> destination.hasRoute<ChatRoute>()
        is SettingsRoute -> destination.hasRoute<SettingsRoute>()
        else -> false
    }
}
