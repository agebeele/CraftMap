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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.components.MinecraftBadge
import com.example.ui.components.MinecraftBlockCard
import com.example.ui.components.MinecraftButton
import com.example.ui.theme.DeepslateCard
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.ExperienceGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GrassGreenPrimary
import com.example.ui.theme.NetherPortalPurple
import com.example.ui.theme.RedstoneAccent
import com.example.ui.theme.StoneOutline

@Composable
fun ConverterScreen(
    activeRealm: GroupRealm?,
    currentGpsLat: Double,
    currentGpsLng: Double,
    currentGpsAlt: Double,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: GPS -> MC, 1: MC -> GPS, 2: Nether Sync

    val spawnLat = activeRealm?.spawnLat ?: currentGpsLat
    val spawnLng = activeRealm?.spawnLng ?: currentGpsLng
    val spawnAlt = activeRealm?.spawnAlt ?: currentGpsAlt

    // Form 1 State (GPS to MC)
    var inputLat by remember { mutableStateOf(String.format("%.6f", currentGpsLat)) }
    var inputLng by remember { mutableStateOf(String.format("%.6f", currentGpsLng)) }
    var inputAlt by remember { mutableStateOf(currentGpsAlt.toInt().toString()) }

    // Form 2 State (MC to GPS)
    var inputX by remember { mutableStateOf("100") }
    var inputY by remember { mutableStateOf("64") }
    var inputZ by remember { mutableStateOf("-250") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF13181E))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "CONVERTIDOR DE COORDENADAS",
                    color = GoldAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "GPS Real ⟷ Minecraft Bloques",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "1 metro real = 1 bloque Minecraft • Spawn [X:0, Y:64, Z:0]",
                    color = Color(0xFFA0AEC0),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Mode Switcher Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1A222B),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = GoldAccent
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("GPS ➔ Bloques", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Bloques ➔ GPS", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("🔥 Nether 1:8", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        // TAB 0: GPS TO MINECRAFT
        if (selectedTab == 0) {
            item {
                MinecraftBlockCard(borderColor = GrassGreenPrimary) {
                    Text(
                        text = "1. Ingresar Coordenadas GPS del Mundo Real",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = inputLat,
                            onValueChange = { inputLat = it },
                            label = { Text("Latitud") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f).testTag("input_converter_lat")
                        )
                        OutlinedTextField(
                            value = inputLng,
                            onValueChange = { inputLng = it },
                            label = { Text("Longitud") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f).testTag("input_converter_lng")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputAlt,
                        onValueChange = { inputAlt = it },
                        label = { Text("Altitud (metros sobre el mar)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    MinecraftButton(
                        text = "Cargar Mi Ubicación GPS Actual",
                        onClick = {
                            inputLat = String.format("%.6f", currentGpsLat)
                            inputLng = String.format("%.6f", currentGpsLng)
                            inputAlt = currentGpsAlt.toInt().toString()
                        },
                        icon = { Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        color = Color(0xFF283648),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Calculation Results
            item {
                val targetLat = inputLat.toDoubleOrNull() ?: spawnLat
                val targetLng = inputLng.toDoubleOrNull() ?: spawnLng
                val targetAlt = inputAlt.toDoubleOrNull() ?: spawnAlt

                val resultCoords = CoordinateConverter.gpsToMinecraft(
                    targetLat = targetLat,
                    targetLng = targetLng,
                    targetAlt = targetAlt,
                    spawnLat = spawnLat,
                    spawnLng = spawnLng,
                    spawnAlt = spawnAlt
                )
                val dist = CoordinateConverter.distanceInBlocks(resultCoords.x, resultCoords.z).toInt()
                val dir = CoordinateConverter.compassDirection(resultCoords.x, resultCoords.z)
                val biome = CoordinateConverter.estimateBiome(resultCoords)

                MinecraftBlockCard(borderColor = GoldAccent, containerColor = Color(0xFF19232D)) {
                    Text(
                        text = "Resultado en Bloques de Minecraft:",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ResultPill(label = "X (Este/Oeste)", value = "${resultCoords.x}", color = RedstoneAccent)
                        ResultPill(label = "Y (Altura)", value = "${resultCoords.y}", color = GrassGreenPrimary)
                        ResultPill(label = "Z (Norte/Sur)", value = "${resultCoords.z}", color = DiamondCyan)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF12171F), RoundedCornerShape(4.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "🌟 Distancia al Spawn: $dist bloques",
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "🧭 Dirección: $dir",
                                color = GoldAccent,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "🔥 En Dimensión Nether: [X: ${resultCoords.netherX}, Z: ${resultCoords.netherZ}]",
                                color = NetherPortalPurple,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "📦 Chunk: [${resultCoords.chunkX}, ${resultCoords.chunkZ}] (offset: ${resultCoords.chunkOffsetLocalX}, ${resultCoords.chunkOffsetLocalZ})",
                                color = Color(0xFF94A3B8),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "🌲 Bioma Estimado: $biome",
                                color = ExperienceGreen,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // TAB 1: MINECRAFT TO GPS
        if (selectedTab == 1) {
            item {
                MinecraftBlockCard(borderColor = DiamondCyan) {
                    Text(
                        text = "1. Ingresar Coordenadas de Minecraft (X, Y, Z)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = inputX,
                            onValueChange = { inputX = it },
                            label = { Text("Bloque X") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f).testTag("input_converter_x")
                        )
                        OutlinedTextField(
                            value = inputY,
                            onValueChange = { inputY = it },
                            label = { Text("Bloque Y") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = inputZ,
                            onValueChange = { inputZ = it },
                            label = { Text("Bloque Z") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f).testTag("input_converter_z")
                        )
                    }
                }
            }

            item {
                val mcX = inputX.toIntOrNull() ?: 0
                val mcY = inputY.toIntOrNull() ?: 64
                val mcZ = inputZ.toIntOrNull() ?: 0

                val gpsResult = CoordinateConverter.minecraftToGps(
                    mcCoords = MinecraftCoords(mcX, mcY, mcZ),
                    spawnLat = spawnLat,
                    spawnLng = spawnLng,
                    spawnAlt = spawnAlt
                )
                val dist = CoordinateConverter.distanceInBlocks(mcX, mcZ).toInt()
                val dir = CoordinateConverter.compassDirection(mcX, mcZ)

                MinecraftBlockCard(borderColor = GoldAccent, containerColor = Color(0xFF19232D)) {
                    Text(
                        text = "Ubicación en el Mundo Real (GPS):",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF12171F), RoundedCornerShape(4.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Latitud: ${String.format("%.7f", gpsResult.latitude)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Longitud: ${String.format("%.7f", gpsResult.longitude)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Altitud: ${gpsResult.altitude.toInt()} metros",
                                color = Color(0xFFCBD5E1),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "🧭 A $dist metros de distancia hacia el $dir respecto al World Spawn",
                                color = DiamondCyan,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // TAB 2: NETHER PORTAL SYNCHRONIZER
        if (selectedTab == 2) {
            item {
                MinecraftBlockCard(borderColor = NetherPortalPurple) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Whatshot, contentDescription = null, tint = NetherPortalPurple)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Regla de Portales del Nether (Relación 1:8)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1 bloque caminado en el Nether equivale a 8 bloques recorridos en el Overworld. Para vincular portales perfectamente:",
                        color = Color(0xFFD1D5DB),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF221124), RoundedCornerShape(4.dp))
                            .border(1.dp, NetherPortalPurple.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Overworld Coords: (X, Y, Z)", color = DiamondCyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text("⬇ División entre 8", color = GoldAccent, fontFamily = FontFamily.Monospace)
                            Text("Nether Coords: (⌊X / 8⌋, Y, ⌊Z / 8⌋)", color = NetherPortalPurple, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Explanatory Educational Card
        item {
            MinecraftBlockCard(borderColor = StoneOutline, containerColor = Color(0xFF171E27)) {
                Text(
                    text = "📐 ¿Cómo funciona la conversión de coordenadas?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• En Minecraft: el eje X va de Oeste (-) a Este (+), mientras que el eje Z va de Norte (-) a Sur (+).\n" +
                            "• El eje Y representa la altura sobre el nivel del mar, donde Y=64 es el nivel del mar estándar de Minecraft.\n" +
                            "• Cada bloque mide exactamente 1 metro. La proyección esférica calcula la distancia precisa en metros desde el World Spawn acordado por tu grupo.",
                    color = Color(0xFFA0AEC0),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun ResultPill(
    label: String,
    value: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .background(Color(0xFF141922), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = Color(0xFFA0AEC0), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text(value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}
