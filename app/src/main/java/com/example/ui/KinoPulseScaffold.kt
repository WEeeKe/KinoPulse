package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.ui.components.MinimalIconButton
import com.example.ui.components.NotificationsBottomSheet
import com.example.ui.icons.LucideIcons
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
import com.example.ui.theme.LocalKinoPalette
import com.example.ui.theme.ThemeMode

private data class TopNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: Any,
    val testTag: String
)

@Composable
fun KinoPulseAppRoot(viewModel: KinoPulseViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = LocalKinoPalette.current
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
            TopNavItem("Главная", LucideIcons.Home, LucideIcons.Home, HomeRoute, "nav_home"),
            TopNavItem("Поиск", LucideIcons.Search, LucideIcons.Search, SearchRoute, "nav_search"),
            TopNavItem("Избранное", LucideIcons.BookmarkFilled, LucideIcons.Bookmark, FavoritesRoute, "nav_favorites"),
            TopNavItem("Клуб", LucideIcons.MessageCircle, LucideIcons.MessageCircle, ChatRoute("global"), "nav_chat"),
            TopNavItem("Профиль", LucideIcons.User, LucideIcons.User, SettingsRoute, "nav_settings")
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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        val useNavRail = maxWidth >= 600.dp

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Minimalist Header Bar
                if (!isFullScreenRoute) {
                    MinimalTopHeader(
                        isOffline = uiState.effectiveOfflineMode,
                        isNight = uiState.preferences.themeMode == ThemeMode.NIGHT,
                        unreadNotifications = uiState.unreadNotificationsCount,
                        onToggleNight = { viewModel.toggleNightMode() },
                        onOpenNotifications = { showNotificationsSheet = true }
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (!isFullScreenRoute && useNavRail) {
                        MinimalSideNavRail(
                            items = topNavItems,
                            currentDestination = currentDestination,
                            onSelect = { item ->
                                if (item.route == FavoritesRoute) favoritesInitialTab = 0
                                navigateTopLevel(item.route)
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = HomeRoute,
                            enterTransition = {
                                fadeIn(animationSpec = tween(220)) +
                                    scaleIn(initialScale = 0.985f, animationSpec = tween(220))
                            },
                            exitTransition = {
                                fadeOut(animationSpec = tween(170))
                            },
                            popEnterTransition = {
                                fadeIn(animationSpec = tween(200))
                            },
                            popExitTransition = {
                                fadeOut(animationSpec = tween(170))
                            }
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
                                    onOpenSearch = { navigateTopLevel(SearchRoute) },
                                    onSyncMoreOnline = { viewModel.syncMoreRealMovies(nextPage = true) }
                                )
                            }

                            composable<SearchRoute> {
                                SearchScreen(
                                    uiState = uiState,
                                    onQueryChange = viewModel::updateSearchQuery,
                                    onToggleGenre = viewModel::toggleGenreFilter,
                                    onSelectYearPreset = viewModel::selectYearPreset,
                                    onCustomYearRange = viewModel::updateCustomYearRange,
                                    onSelectMinRating = viewModel::selectMinRating,
                                    onSelectDuration = viewModel::selectDurationFilter,
                                    onSelectSort = viewModel::updateSortOption,
                                    onToggleOfflineOnly = viewModel::toggleOfflineOnlyFilter,
                                    onToggleFavoritesOnly = viewModel::toggleFavoritesOnlyFilter,
                                    onToggleHdOnly = viewModel::toggleHdOnlyFilter,
                                    onResetFilters = viewModel::resetSearchFilters,
                                    onResetAll = viewModel::resetAllSearchAndQuery,
                                    onSelectCardLayout = viewModel::setCardLayoutMode,
                                    onSetShowRating = viewModel::setShowRatingOnCards,
                                    onSetShowMeta = viewModel::setShowMetaOnCards,
                                    onSetShowGenres = viewModel::setShowGenresOnCards,
                                    onSearchOnlineArchive = viewModel::searchOnlineArchive,
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
                                    onToggleLike = viewModel::toggleChatLike,
                                    bottomDockClearance = if (useNavRail) 12.dp else 94.dp
                                )
                            }

                            composable<SettingsRoute> {
                                SettingsScreen(
                                    uiState = uiState,
                                    onSetThemeMode = viewModel::setThemeMode,
                                    onSetAmoledNight = viewModel::setAmoledNightMode,
                                    onSetCardLayoutMode = viewModel::setCardLayoutMode,
                                    onSetShowRatingOnCards = viewModel::setShowRatingOnCards,
                                    onSetShowMetaOnCards = viewModel::setShowMetaOnCards,
                                    onSetShowGenresOnCards = viewModel::setShowGenresOnCards,
                                    onSetOfflineMode = viewModel::setOfflineMode,
                                    onSetNotificationsEnabled = viewModel::setNotificationsEnabled,
                                    onTriggerNotification = viewModel::triggerNewReleaseNotification,
                                    onSyncMoreOnline = { viewModel.syncMoreRealMovies(nextPage = true) },
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
                                        onGenreClick = { genre ->
                                            viewModel.selectSingleGenreAndReset(genre)
                                            navigateTopLevel(SearchRoute)
                                        },
                                        onOpenRelatedMovie = { relatedId ->
                                            navController.navigate(MovieDetailRoute(relatedId))
                                        },
                                        onPlayRelatedMovie = { relatedId ->
                                            navController.navigate(PlayerRoute(relatedId))
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
                                        resolveVideoUrl = { target -> viewModel.resolvePlayableVideoUrl(target) },
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

            // Minimalist Snackbar floating neatly above the dock
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = if (!isFullScreenRoute && !useNavRail) 92.dp else 18.dp, start = 18.dp, end = 18.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = palette.surfaceElevated,
                    contentColor = palette.textPrimary,
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Floating Frosted-Glass Bottom Navigation Dock (Non-Material-You)
            if (!isFullScreenRoute && !useNavRail) {
                FloatingMinimalBottomBar(
                    items = topNavItems,
                    currentDestination = currentDestination,
                    onSelect = { item ->
                        if (item.route == FavoritesRoute) favoritesInitialTab = 0
                        navigateTopLevel(item.route)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
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

@Composable
private fun MinimalTopHeader(
    isOffline: Boolean,
    isNight: Boolean,
    unreadNotifications: Int,
    onToggleNight: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    val palette = LocalKinoPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.background)
            .statusBarsPadding()
            .padding(start = 18.dp, end = 14.dp, top = 8.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(palette.accent)
            )
            Text(
                text = "KinoPulse",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )
            if (isOffline) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(palette.surface)
                        .border(1.dp, palette.borderSubtle, RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.WifiOff,
                        contentDescription = null,
                        tint = palette.accent,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Офлайн",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textSecondary
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MinimalIconButton(
                icon = if (isNight) LucideIcons.Sun else LucideIcons.Moon,
                contentDescription = "Переключить ночной режим",
                onClick = onToggleNight,
                size = 40.dp,
                iconSize = 19.dp,
                modifier = Modifier.testTag("topbar_night_toggle")
            )
            MinimalIconButton(
                icon = LucideIcons.Bell,
                contentDescription = "Уведомления о новинках",
                onClick = onOpenNotifications,
                size = 40.dp,
                iconSize = 19.dp,
                showDot = unreadNotifications > 0,
                modifier = Modifier.testTag("topbar_notifications_button")
            )
        }
    }
}

@Composable
private fun FloatingMinimalBottomBar(
    items: List<TopNavItem>,
    currentDestination: NavDestination?,
    onSelect: (TopNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalKinoPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (palette.isDark) 18.dp else 10.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = Color.Black.copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(palette.surfaceGlass)
            .border(1.dp, palette.border, RoundedCornerShape(26.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val selected = isRouteSelected(currentDestination, item.route)
            val iconTint by animateColorAsState(
                targetValue = if (selected) palette.accent else palette.textTertiary,
                label = "nav_tint_${item.testTag}"
            )
            val labelColor by animateColorAsState(
                targetValue = if (selected) palette.textPrimary else palette.textTertiary,
                label = "nav_label_${item.testTag}"
            )
            val scale by animateFloatAsState(
                targetValue = if (selected) 1.06f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "nav_scale_${item.testTag}"
            )
            val interactionSource = remember { MutableInteractionSource() }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (selected) palette.surfaceElevated.copy(alpha = 0.72f) else Color.Transparent)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onSelect(item) }
                    )
                    .padding(vertical = 6.dp, horizontal = 4.dp)
                    .testTag(item.testTag),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.label,
                    tint = iconTint,
                    modifier = Modifier
                        .size(21.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = labelColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MinimalSideNavRail(
    items: List<TopNavItem>,
    currentDestination: NavDestination?,
    onSelect: (TopNavItem) -> Unit
) {
    val palette = LocalKinoPalette.current
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(84.dp)
            .background(palette.surface)
            .border(1.dp, palette.borderSubtle)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEach { item ->
            val selected = isRouteSelected(currentDestination, item.route)
            val tint by animateColorAsState(
                targetValue = if (selected) palette.accent else palette.textTertiary,
                label = "rail_tint_${item.testTag}"
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selected) palette.surfaceElevated else Color.Transparent)
                    .clickable { onSelect(item) }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .testTag(item.testTag),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.label,
                    tint = tint,
                    modifier = Modifier.size(21.dp)
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) palette.textPrimary else palette.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

private fun isRouteSelected(
    destination: NavDestination?,
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
