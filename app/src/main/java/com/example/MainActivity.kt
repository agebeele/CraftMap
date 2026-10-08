package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.components.MinecraftBadge
import com.example.ui.screens.ConverterScreen
import com.example.ui.screens.GroupsScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.WaypointsScreen
import com.example.ui.theme.DeepslateCard
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GrassGreenPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RedstoneAccent
import com.example.ui.util.NotificationHelper
import kotlinx.coroutines.launch

enum class AppNavTab(val title: String, val icon: ImageVector, val tag: String) {
    MAP("Mapa", Icons.Default.Map, "nav_tab_map"),
    GROUPS("Reinos", Icons.Default.Group, "nav_tab_groups"),
    WAYPOINTS("Pines", Icons.Default.Place, "nav_tab_waypoints"),
    CONVERTER("Conversor", Icons.Default.Calculate, "nav_tab_converter"),
    PROFILE("Perfil", Icons.Default.Person, "nav_tab_profile")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CraftMapApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CraftMapApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(AppNavTab.MAP) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // State bindings
    val currentUserCoords by viewModel.currentUserCoords.collectAsStateWithLifecycle()
    val userLat by viewModel.userLat.collectAsStateWithLifecycle()
    val userLng by viewModel.userLng.collectAsStateWithLifecycle()
    val userAlt by viewModel.userAlt.collectAsStateWithLifecycle()
    val cameraTarget by viewModel.mapCameraCenter.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val activeRealm by viewModel.activeRealm.collectAsStateWithLifecycle()
    val allRealms by viewModel.allRealms.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val waypoints by viewModel.waypoints.collectAsStateWithLifecycle()
    val invites by viewModel.invites.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    // Initialize notification channels
    LaunchedEffect(Unit) {
        NotificationHelper.createNotificationChannel(context)
    }

    // Handle snackbars
    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Permission launcher for fine/coarse GPS location and POST_NOTIFICATIONS
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            viewModel.fetchCurrentGpsOnce()
        }
    }

    // Initial permissions check (Location & Notifications)
    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (fineGranted) {
            viewModel.fetchCurrentGpsOnce()
        }

        val neededPermissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val notifGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!notifGranted) {
                neededPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (!fineGranted || neededPermissions.size > 2) {
            permissionLauncher.launch(neededPermissions.toTypedArray())
        }
    }

    // BackHandler: sub-screens go back to MAP
    BackHandler(enabled = currentTab != AppNavTab.MAP) {
        currentTab = AppNavTab.MAP
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(GrassGreenPrimary, RoundedCornerShape(4.dp))
                                .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CraftMap",
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 19.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        activeRealm?.let { realm ->
                            MinecraftBadge(
                                text = realm.code,
                                color = GoldAccent
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val fineGranted = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            if (fineGranted) {
                                viewModel.fetchCurrentGpsOnce()
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        modifier = Modifier.testTag("btn_top_gps")
                    ) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "GPS",
                            tint = DiamondCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF131820),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF151B22),
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = Color(0xFF242C38),
                    shape = RoundedCornerShape(0.dp)
                )
            ) {
                AppNavTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = GoldAccent,
                            indicatorColor = GrassGreenPrimary,
                            unselectedIconColor = Color(0xFF7E8B9B),
                            unselectedTextColor = Color(0xFF7E8B9B)
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppNavTab.MAP -> MapScreen(
                    currentUserCoords = currentUserCoords,
                    userGpsLat = userLat,
                    userGpsLng = userLng,
                    cameraTarget = cameraTarget,
                    userProfile = userProfile,
                    activeRealm = activeRealm,
                    members = members,
                    waypoints = waypoints,
                    onAddWaypoint = { title, desc, type, coords ->
                        viewModel.addWaypoint(title, desc, type, targetCoords = coords)
                    },
                    onDeleteWaypoint = { id -> viewModel.deleteWaypoint(id) },
                    onTeleportToSpawn = { viewModel.teleportToSpawn() },
                    onRefreshGps = { viewModel.fetchCurrentGpsOnce() },
                    onSimulateMove = { dLat, dLng -> viewModel.setSimulationLocationOffset(dLat, dLng) },
                    onNavigateToGroups = { currentTab = AppNavTab.GROUPS }
                )
                AppNavTab.GROUPS -> GroupsScreen(
                    activeRealm = activeRealm,
                    allRealms = allRealms,
                    members = members,
                    invites = invites,
                    userProfile = userProfile,
                    currentUserCoords = currentUserCoords,
                    userGpsLat = userLat,
                    userGpsLng = userLng,
                    userGpsAlt = userAlt,
                    onCreateRealm = { name, desc, useCurrent, lat, lng, alt, password ->
                        viewModel.createRealm(name, desc, useCurrent, lat, lng, alt, password)
                    },
                    onJoinRealm = { code, password -> viewModel.joinRealm(code, password) },
                    onSwitchRealm = { id -> viewModel.switchActiveRealm(id) },
                    onSendInvite = { tag, realm -> viewModel.sendRealmInvite(tag, realm, context) }
                )
                AppNavTab.WAYPOINTS -> WaypointsScreen(
                    waypoints = waypoints,
                    currentUserCoords = currentUserCoords,
                    activeRealm = activeRealm,
                    onAddWaypoint = { title, desc, type, coords ->
                        viewModel.addWaypoint(title, desc, type, targetCoords = coords)
                    },
                    onDeleteWaypoint = { id -> viewModel.deleteWaypoint(id) },
                    onNavigateToMap = { currentTab = AppNavTab.MAP }
                )
                AppNavTab.CONVERTER -> ConverterScreen(
                    activeRealm = activeRealm,
                    currentGpsLat = userLat,
                    currentGpsLng = userLng,
                    currentGpsAlt = userAlt
                )
                AppNavTab.PROFILE -> ProfileScreen(
                    userProfile = userProfile,
                    currentUserCoords = currentUserCoords,
                    activeRealm = activeRealm,
                    onSaveProfile = { name, preset, uri, mcUser ->
                        viewModel.updateProfile(name, preset, uri, mcUser)
                    },
                    onRemoveCustomSkin = { viewModel.removeCustomSkin() }
                )
            }
        }
    }
}
