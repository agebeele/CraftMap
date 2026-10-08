package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.converter.CoordinateConverter
import com.example.data.model.MinecraftCoords
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GrassGreenPrimary
import com.example.ui.theme.NetherPortalPurple
import com.example.ui.theme.RedstoneAccent

@Composable
fun MinecraftCoordinateHud(
    coords: MinecraftCoords,
    userGpsLat: Double,
    userGpsLng: Double,
    spawnLat: Double,
    spawnLng: Double,
    realmName: String,
    onToggleVisibility: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val distToSpawn = CoordinateConverter.distanceInBlocks(coords.x, coords.z)
    val facing = CoordinateConverter.compassDirection(coords.x, coords.z)
    val biome = CoordinateConverter.estimateBiome(coords)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, Color(0xFF2E3846), RoundedCornerShape(6.dp))
            .background(Color(0xE614181F), RoundedCornerShape(6.dp))
            .clickable { expanded = !expanded }
            .padding(10.dp)
            .testTag("minecraft_coord_hud")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(GrassGreenPrimary, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "COORDENADAS MINECRAFT",
                        color = Color(0xFFA0AEC0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MinecraftBadge(
                        text = "$realmName",
                        color = GoldAccent
                    )

                    if (onToggleVisibility != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF283445),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF42536A)),
                            modifier = Modifier
                                .clickable { onToggleVisibility() }
                                .testTag("btn_collapse_coords")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.VisibilityOff,
                                    contentDescription = "Ocultar",
                                    tint = Color(0xFFE2E8F0),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Ocultar",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main X Y Z Block Coords (Signature MC Style)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoordValuePill(label = "X", value = "${coords.x}", color = RedstoneAccent)
                CoordValuePill(label = "Y", value = "${coords.y}", color = GrassGreenPrimary)
                CoordValuePill(label = "Z", value = "${coords.z}", color = DiamondCyan)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Info Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌟 Spawn: ${distToSpawn.toInt()} bloques",
                    color = Color(0xFFD1D5DB),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "🧭 Hacia: $facing",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFF283340))
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Dimensión Nether: [X: ${coords.netherX}, Y: ${coords.y}, Z: ${coords.netherZ}]",
                        color = NetherPortalPurple,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Chunk: [${coords.chunkX}, ${coords.chunkZ}] (En chunk: ${coords.chunkOffsetLocalX}, ${coords.chunkOffsetLocalZ})",
                        color = Color(0xFF9CA3AF),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Bioma Estimado: $biome",
                        color = ExperienceGreenColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "GPS Real: ${String.format("%.6f", userGpsLat)}, ${String.format("%.6f", userGpsLng)}",
                        color = Color(0xFF6B7280),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

val ExperienceGreenColor = Color(0xFF82F038)

@Composable
fun CoordValuePill(
    label: String,
    value: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .background(Color(0xFF1E242D), RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$label: ",
                color = color,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 15.sp
            )
        }
    }
}
