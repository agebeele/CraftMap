package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.MinecraftCoords
import com.example.data.model.UserProfile
import com.example.data.model.Waypoint
import com.example.ui.theme.DeepslateCard
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GrassGreenPrimary
import com.example.ui.theme.NetherPortalPurple
import com.example.ui.theme.RedstoneAccent

sealed interface SelectedMapItem {
    data class WaypointItem(val waypoint: Waypoint) : SelectedMapItem
    data class SpawnItem(val lat: Double, val lng: Double, val alt: Double, val realmName: String) : SelectedMapItem
    data class PlayerItem(val member: GroupMember) : SelectedMapItem
    data class CurrentUserItem(val profile: UserProfile, val coords: MinecraftCoords, val lat: Double, val lng: Double) : SelectedMapItem
    data class InspectedBlockItem(val coords: MinecraftCoords, val lat: Double, val lng: Double) : SelectedMapItem
}

@Composable
fun MapItemDetailCard(
    item: SelectedMapItem,
    currentUserCoords: MinecraftCoords,
    onDismiss: () -> Unit,
    onAddPinHere: (MinecraftCoords, Double, Double) -> Unit,
    onDeleteWaypoint: (String) -> Unit,
    onTeleportToSpawn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xF2151B24),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            when (item) {
                is SelectedMapItem.SpawnItem -> GoldAccent
                is SelectedMapItem.WaypointItem -> item.waypoint.type.getColor()
                is SelectedMapItem.PlayerItem -> DiamondCyan
                is SelectedMapItem.CurrentUserItem -> GrassGreenPrimary
                is SelectedMapItem.InspectedBlockItem -> GoldAccent
            }
        ),
        shadowElevation = 12.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("map_item_detail_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    val iconText = when (item) {
                        is SelectedMapItem.SpawnItem -> "🌟"
                        is SelectedMapItem.WaypointItem -> item.waypoint.type.iconEmoji
                        is SelectedMapItem.PlayerItem -> "👤"
                        is SelectedMapItem.CurrentUserItem -> "🟢"
                        is SelectedMapItem.InspectedBlockItem -> "📍"
                    }
                    Text(iconText, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        val titleText = when (item) {
                            is SelectedMapItem.SpawnItem -> "World Spawn (0, 64, 0)"
                            is SelectedMapItem.WaypointItem -> item.waypoint.title
                            is SelectedMapItem.PlayerItem -> "@${item.member.gamertag}"
                            is SelectedMapItem.CurrentUserItem -> "@${item.profile.gamertag} (Tú)"
                            is SelectedMapItem.InspectedBlockItem -> "Bloque del Mundo Real"
                        }
                        val subtitleText = when (item) {
                            is SelectedMapItem.SpawnItem -> "Origen oficial de ${item.realmName}"
                            is SelectedMapItem.WaypointItem -> "${item.waypoint.type.labelEs} • Creado por @${item.waypoint.creatorGamertag}"
                            is SelectedMapItem.PlayerItem -> "Estado: ${item.member.status}"
                            is SelectedMapItem.CurrentUserItem -> "Tu posición en tiempo real"
                            is SelectedMapItem.InspectedBlockItem -> "Coordenadas calculadas en este punto"
                        }
                        Text(
                            text = titleText,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = subtitleText,
                            color = Color(0xFFA0AEC0),
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF283445),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF455770)),
                    modifier = Modifier
                        .clickable(onClick = onDismiss)
                        .testTag("btn_close_item_detail")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Cerrar",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Coordinates Box (Minecraft Block X, Y, Z & Nether)
            val mcCoords = when (item) {
                is SelectedMapItem.SpawnItem -> MinecraftCoords(0, 64, 0)
                is SelectedMapItem.WaypointItem -> MinecraftCoords(item.waypoint.mcX, item.waypoint.mcY, item.waypoint.mcZ)
                is SelectedMapItem.PlayerItem -> MinecraftCoords(item.member.mcX, item.member.mcY, item.member.mcZ)
                is SelectedMapItem.CurrentUserItem -> item.coords
                is SelectedMapItem.InspectedBlockItem -> item.coords
            }

            val (gpsLat, gpsLng) = when (item) {
                is SelectedMapItem.SpawnItem -> Pair(item.lat, item.lng)
                is SelectedMapItem.WaypointItem -> Pair(item.waypoint.gpsLat, item.waypoint.gpsLng)
                is SelectedMapItem.PlayerItem -> Pair(item.member.lat, item.member.lng)
                is SelectedMapItem.CurrentUserItem -> Pair(item.lat, item.lng)
                is SelectedMapItem.InspectedBlockItem -> Pair(item.lat, item.lng)
            }

            val dist = CoordinateConverter.distanceInBlocks(
                mcCoords.x, mcCoords.z,
                currentUserCoords.x, currentUserCoords.z
            ).toInt()

            val facing = CoordinateConverter.compassDirection(
                mcCoords.x, mcCoords.z,
                currentUserCoords.x, currentUserCoords.z
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3948), RoundedCornerShape(6.dp))
                    .background(Color(0xFF10151C), RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Bloques: [X:${mcCoords.x}, Y:${mcCoords.y}, Z:${mcCoords.z}]",
                            color = DiamondCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Nether: [${mcCoords.netherX}, ${mcCoords.netherZ}]",
                            color = NetherPortalPurple,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🌟 A $dist bloques de distancia ($facing)",
                            color = GoldAccent,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "GPS Real: ${String.format("%.6f", gpsLat)}, ${String.format("%.6f", gpsLng)}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    if (item is SelectedMapItem.WaypointItem && item.waypoint.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Notas: \"${item.waypoint.description}\"",
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            when (item) {
                is SelectedMapItem.WaypointItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MinecraftButton(
                            text = "Fijar Rumbo",
                            onClick = onDismiss,
                            icon = { Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = {
                                onDeleteWaypoint(item.waypoint.id)
                                onDismiss()
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, RedstoneAccent),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RedstoneAccent),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Borrar", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = onDismiss,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF45556B)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Cerrar", fontSize = 12.sp)
                        }
                    }
                }
                is SelectedMapItem.SpawnItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MinecraftButton(
                            text = "Ubicarse en Spawn (0,0)",
                            onClick = {
                                onTeleportToSpawn()
                                onDismiss()
                            },
                            icon = { Icon(Icons.Default.Stars, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            color = GoldAccent,
                            textColor = Color(0xFF131820),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = onDismiss,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF45556B)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Cerrar", fontSize = 12.sp)
                        }
                    }
                }
                is SelectedMapItem.InspectedBlockItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MinecraftButton(
                            text = "Fijar Pin Aquí (Casa/Mina)",
                            onClick = {
                                onAddPinHere(item.coords, item.lat, item.lng)
                                onDismiss()
                            },
                            icon = { Icon(Icons.Default.AddLocation, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = onDismiss,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF45556B)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Cerrar", fontSize = 12.sp)
                        }
                    }
                }
                else -> {
                    MinecraftButton(
                        text = "✕ Cerrar Información",
                        onClick = onDismiss,
                        color = Color(0xFF283446),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
