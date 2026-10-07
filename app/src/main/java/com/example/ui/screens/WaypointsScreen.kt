package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.GroupRealm
import com.example.data.model.MinecraftCoords
import com.example.data.model.Waypoint
import com.example.data.model.WaypointType
import com.example.ui.components.MinecraftBadge
import com.example.ui.components.MinecraftBlockCard
import com.example.ui.components.MinecraftButton
import com.example.ui.theme.DeepslateCard
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GrassGreenPrimary
import com.example.ui.theme.NetherPortalPurple
import com.example.ui.theme.RedstoneAccent
import com.example.ui.theme.StoneOutline

@Composable
fun WaypointsScreen(
    waypoints: List<Waypoint>,
    currentUserCoords: MinecraftCoords,
    activeRealm: GroupRealm?,
    onAddWaypoint: (title: String, desc: String, type: WaypointType, coords: MinecraftCoords?) -> Unit,
    onDeleteWaypoint: (waypointId: String) -> Unit,
    onNavigateToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<WaypointType?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var waypointToDelete by remember { mutableStateOf<Waypoint?>(null) }

    val filteredWaypoints = if (selectedFilter == null) {
        waypoints
    } else {
        waypoints.filter { it.type == selectedFilter }
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF13181E))) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WAYPOINTS Y PINES",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Puntos de Interés (${waypoints.size})",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    MinecraftButton(
                        text = "+ Nuevo Pin",
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("btn_add_pin_screen")
                    )
                }
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterPill(
                            label = "Todos",
                            emoji = "🗺️",
                            isSelected = selectedFilter == null,
                            onClick = { selectedFilter = null }
                        )
                    }
                    items(WaypointType.values()) { type ->
                        FilterPill(
                            label = type.labelEs,
                            emoji = type.iconEmoji,
                            isSelected = selectedFilter == type,
                            onClick = { selectedFilter = type }
                        )
                    }
                }
            }

            if (filteredWaypoints.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📍", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No hay pines en esta categoría",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Toca '+ Nuevo Pin' para guardar tu casa o base",
                                color = Color(0xFFA0AEC0),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredWaypoints) { wp ->
                    WaypointCard(
                        waypoint = wp,
                        currentUserCoords = currentUserCoords,
                        onDelete = { waypointToDelete = wp },
                        onViewOnMap = onNavigateToMap
                    )
                }
            }
        }

        // Add Pin Dialog
        if (showAddDialog) {
            AddWaypointDialog(
                defaultCoords = currentUserCoords,
                onDismiss = { showAddDialog = false },
                onConfirm = { title, desc, type ->
                    onAddWaypoint(title, desc, type, currentUserCoords)
                    showAddDialog = false
                }
            )
        }

        // Delete Confirm Dialog
        waypointToDelete?.let { wp ->
            AlertDialog(
                onDismissRequest = { waypointToDelete = null },
                containerColor = DeepslateCard,
                title = { Text("¿Eliminar Pin?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "¿Estás seguro de que deseas eliminar '${wp.title}'? Esta acción se sincronizará con todos los compañeros.",
                        color = Color(0xFFD1D5DB)
                    )
                },
                confirmButton = {
                    MinecraftButton(
                        text = "Eliminar",
                        onClick = {
                            onDeleteWaypoint(wp.id)
                            waypointToDelete = null
                        },
                        color = RedstoneAccent
                    )
                },
                dismissButton = {
                    TextButton(onClick = { waypointToDelete = null }) {
                        Text("Cancelar", color = Color(0xFFA0AEC0))
                    }
                }
            )
        }
    }
}

@Composable
fun FilterPill(
    label: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) GoldAccent else StoneOutline,
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = if (isSelected) GoldAccent.copy(alpha = 0.2f) else Color(0xFF1B222C),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = if (isSelected) Color.White else Color(0xFFA0AEC0),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun WaypointCard(
    waypoint: Waypoint,
    currentUserCoords: MinecraftCoords,
    onDelete: () -> Unit,
    onViewOnMap: () -> Unit
) {
    val dist = CoordinateConverter.distanceInBlocks(
        waypoint.mcX, waypoint.mcZ,
        currentUserCoords.x, currentUserCoords.z
    ).toInt()

    val facing = CoordinateConverter.compassDirection(
        waypoint.mcX, waypoint.mcZ,
        currentUserCoords.x, currentUserCoords.z
    )

    MinecraftBlockCard(
        borderColor = waypoint.type.getColor().copy(alpha = 0.6f),
        containerColor = Color(0xFF1A212B),
        modifier = Modifier.testTag("waypoint_item_${waypoint.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Text(waypoint.type.iconEmoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = waypoint.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Creado por: ${waypoint.creatorGamertag}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = RedstoneAccent.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (waypoint.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = waypoint.description,
                color = Color(0xFFCBD5E1),
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Minecraft Coordinates Row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF2C3949), RoundedCornerShape(4.dp))
                .background(Color(0xFF141920), RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Bloques: [X:${waypoint.mcX}, Y:${waypoint.mcY}, Z:${waypoint.mcZ}]",
                        color = DiamondCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Nether: [${waypoint.mcX / 8}, ${waypoint.mcZ / 8}]",
                        color = NetherPortalPurple,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🧭 $dist bloques ($facing)",
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "GPS: ${String.format("%.4f", waypoint.gpsLat)}, ${String.format("%.4f", waypoint.gpsLng)}",
                        color = Color(0xFF6B7280),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
