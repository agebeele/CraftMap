package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.converter.CoordinateConverter
import com.example.data.model.GroupMember
import com.example.data.model.GroupRealm
import com.example.data.model.MinecraftCoords
import com.example.data.model.UserProfile
import com.example.data.model.Waypoint
import com.example.data.model.WaypointType
import com.example.ui.components.MinecraftBlockCard
import com.example.ui.components.MinecraftButton
import com.example.ui.components.MinecraftCoordinateHud
import com.example.ui.components.SkinAvatar
import com.example.ui.map.RealWorldMapView
import com.example.ui.map.VoxelMapView
import com.example.ui.theme.DeepslateCard
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GrassGreenPrimary
import com.example.ui.theme.RedstoneAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    currentUserCoords: MinecraftCoords,
    userGpsLat: Double,
    userGpsLng: Double,
    userProfile: UserProfile,
    activeRealm: GroupRealm?,
    members: List<GroupMember>,
    waypoints: List<Waypoint>,
    onAddWaypoint: (title: String, desc: String, type: WaypointType, coords: MinecraftCoords?) -> Unit,
    onRefreshGps: () -> Unit,
    onSimulateMove: (dLat: Double, dLng: Double) -> Unit,
    onNavigateToGroups: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRealWorldMapMode by remember { mutableStateOf(true) } // Default to real-world map with satellite/streets!
    var showAddDialog by remember { mutableStateOf(false) }
    var preselectedCoords by remember { mutableStateOf<MinecraftCoords?>(null) }
    var showSimControls by remember { mutableStateOf(false) }

    val spawnLat = activeRealm?.spawnLat ?: userGpsLat
    val spawnLng = activeRealm?.spawnLng ?: userGpsLng
    val spawnAlt = activeRealm?.spawnAlt ?: 0.0

    Box(modifier = modifier.fillMaxSize()) {
        // Map Renderer based on mode: Real World (Satellite / Streets) vs Voxel Canvas
        if (isRealWorldMapMode) {
            RealWorldMapView(
                centerLat = userGpsLat,
                centerLng = userGpsLng,
                spawnLat = spawnLat,
                spawnLng = spawnLng,
                spawnAlt = spawnAlt,
                userGpsLat = userGpsLat,
                userGpsLng = userGpsLng,
                userProfile = userProfile,
                currentUserCoords = currentUserCoords,
                members = members,
                waypoints = waypoints,
                onAddWaypointAtCoords = { coords, _, _ ->
                    preselectedCoords = coords
                    showAddDialog = true
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            VoxelMapView(
                currentUserCoords = currentUserCoords,
                userProfile = userProfile,
                members = members,
                waypoints = waypoints,
                onAddWaypointAtCoords = { coords ->
                    preselectedCoords = coords
                    showAddDialog = true
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top Overlay: HUD and Quick Realm bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Coordinate HUD
            MinecraftCoordinateHud(
                coords = currentUserCoords,
                userGpsLat = userGpsLat,
                userGpsLng = userGpsLng,
                spawnLat = spawnLat,
                spawnLng = spawnLng,
                realmName = activeRealm?.name ?: "Sin Reino"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Realm Bar & Controls Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xD918202A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C3949)),
                    modifier = Modifier.clickable { onNavigateToGroups() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = "Compañeros",
                            tint = DiamondCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${members.size + 1} Jugadores",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Mode Toggle: Real World Maps ⟷ Voxel Canvas
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isRealWorldMapMode) GrassGreenPrimary.copy(alpha = 0.25f) else Color(0xD918202A),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isRealWorldMapMode) GrassGreenPrimary else Color(0xFF2C3949)
                        ),
                        modifier = Modifier
                            .clickable { isRealWorldMapMode = !isRealWorldMapMode }
                            .testTag("btn_toggle_map_mode")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isRealWorldMapMode) Icons.Default.Public else Icons.Default.GridOn,
                                contentDescription = "Modo de Mapa",
                                tint = if (isRealWorldMapMode) GrassGreenPrimary else GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isRealWorldMapMode) "Mundo Real" else "Vóxel 2D",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Walk simulation toggle button
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (showSimControls) GoldAccent.copy(alpha = 0.2f) else Color(0xD918202A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (showSimControls) GoldAccent else Color(0xFF2C3949)),
                        modifier = Modifier.clickable { showSimControls = !showSimControls }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DirectionsWalk,
                                contentDescription = "Simulador de Paseo",
                                tint = if (showSimControls) GoldAccent else Color(0xFFA0AEC0),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Refresh GPS
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xD918202A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C3949)),
                        modifier = Modifier.clickable { onRefreshGps() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Actualizar GPS",
                                tint = GrassGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Walk Simulator Pad for testing GPS in emulator
            AnimatedVisibility(visible = showSimControls) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xF2161D26),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎮 CONTROL DE CAMINATA SIMULADA (Moverse en el Mapa)",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MinecraftButton(
                                text = "▲ N (-15m)",
                                onClick = { onSimulateMove(0.00015, 0.0) },
                                modifier = Modifier.height(36.dp),
                                color = Color(0xFF2E3846)
                            )
                            MinecraftButton(
                                text = "▼ S (+15m)",
                                onClick = { onSimulateMove(-0.00015, 0.0) },
                                modifier = Modifier.height(36.dp),
                                color = Color(0xFF2E3846)
                            )
                            MinecraftButton(
                                text = "◀ O (-15m)",
                                onClick = { onSimulateMove(0.0, -0.00015) },
                                modifier = Modifier.height(36.dp),
                                color = Color(0xFF2E3846)
                            )
                            MinecraftButton(
                                text = "▶ E (+15m)",
                                onClick = { onSimulateMove(0.0, 0.00015) },
                                modifier = Modifier.height(36.dp),
                                color = Color(0xFF2E3846)
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button: Add Waypoint Pin
        ExtendedFloatingActionButton(
            onClick = {
                preselectedCoords = null
                showAddDialog = true
            },
            icon = {
                Icon(
                    Icons.Default.AddLocation,
                    contentDescription = null,
                    tint = Color.White
                )
            },
            text = {
                Text(
                    text = "Añadir Pin / Casa",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            containerColor = GrassGreenPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .testTag("fab_add_waypoint")
        )

        // Dialog: Create Waypoint Pin
        if (showAddDialog) {
            AddWaypointDialog(
                defaultCoords = preselectedCoords ?: currentUserCoords,
                onDismiss = {
                    showAddDialog = false
                    preselectedCoords = null
                },
                onConfirm = { title, desc, type ->
                    onAddWaypoint(title, desc, type, preselectedCoords)
                    showAddDialog = false
                    preselectedCoords = null
                }
            )
        }
    }
}

@Composable
fun AddWaypointDialog(
    defaultCoords: MinecraftCoords,
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, type: WaypointType) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(WaypointType.HOME) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepslateCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${selectedType.iconEmoji} Añadir Pin al Mapa",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Coordenadas fijadas: [X: ${defaultCoords.x}, Y: ${defaultCoords.y}, Z: ${defaultCoords.z}]",
                    color = DiamondCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Tipo de Ubicación:",
                    color = Color(0xFFD1D5DB),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(WaypointType.values()) { type ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) GoldAccent else Color(0xFF384353),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .background(
                                    color = if (isSelected) GoldAccent.copy(alpha = 0.2f) else Color(0xFF1E252E),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { selectedType = type }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(type.iconEmoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = type.labelEs,
                                    color = if (isSelected) Color.White else Color(0xFFA0AEC0),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre del Pin (Ej: Mi Casa, Mina Norte)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GrassGreenPrimary,
                        unfocusedBorderColor = Color(0xFF404D5E)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_pin_title")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción o notas (opcional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GrassGreenPrimary,
                        unfocusedBorderColor = Color(0xFF404D5E)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            MinecraftButton(
                text = "Guardar Pin",
                onClick = { onConfirm(title, description, selectedType) },
                color = GrassGreenPrimary,
                modifier = Modifier.testTag("btn_save_pin")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color(0xFFA0AEC0))
            }
        }
    )
}
