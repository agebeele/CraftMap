package com.example.ui.map

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.ui.components.MinecraftButton
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GrassGreenPrimary
import com.example.ui.theme.NetherPortalPurple
import com.example.ui.theme.RedstoneAccent
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun VoxelMapView(
    currentUserCoords: MinecraftCoords,
    userProfile: UserProfile,
    members: List<GroupMember>,
    waypoints: List<Waypoint>,
    onAddWaypointAtCoords: (MinecraftCoords) -> Unit,
    modifier: Modifier = Modifier
) {
    // Map State: Pan offset (in screen pixels) and Zoom scale (pixels per Minecraft block)
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }
    var pixelsPerBlock by remember { mutableFloatStateOf(0.9f) } // default 1 block = 0.9dp/px
    var isNetherView by remember { mutableStateOf(false) }

    // Tap inspection state
    var selectedPointCoords by remember { mutableStateOf<MinecraftCoords?>(null) }

    val activeScale = if (isNetherView) pixelsPerBlock * 8f else pixelsPerBlock

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isNetherView) Color(0xFF1F0D15) else Color(0xFF13191F))
            .testTag("voxel_map_container")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panOffsetX += dragAmount.x
                        panOffsetY += dragAmount.y
                    }
                }
                .pointerInput(activeScale, panOffsetX, panOffsetY) {
                    detectTapGestures { tapOffset ->
                        val centerX = size.width / 2f + panOffsetX
                        val centerY = size.height / 2f + panOffsetY

                        // In canvas: dx = tap.x - centerX => mcX = dx / scale
                        // dy = tap.y - centerY => In MC, South is +Z (down), North is -Z (up)
                        // so mcZ = dy / scale
                        val mcX = ((tapOffset.x - centerX) / activeScale).roundToInt()
                        val mcZ = ((tapOffset.y - centerY) / activeScale).roundToInt()
                        selectedPointCoords = MinecraftCoords(mcX, 64, mcZ)
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val centerOriginX = canvasW / 2f + panOffsetX
            val centerOriginY = canvasH / 2f + panOffsetY

            // 1. Draw Voxel / Chunk Grid (16 blocks = 1 Chunk)
            drawMinecraftGrid(
                originX = centerOriginX,
                originY = centerOriginY,
                canvasWidth = canvasW,
                canvasHeight = canvasH,
                scale = activeScale,
                isNether = isNetherView
            )

            // 2. Draw World Spawn Beacon at (0, 0)
            drawWorldSpawnBeacon(
                screenX = centerOriginX,
                screenY = centerOriginY,
                isNether = isNetherView
            )

            // 3. Draw Waypoints
            waypoints.forEach { wp ->
                val targetX = if (isNetherView) wp.mcX / 8f else wp.mcX.toFloat()
                val targetZ = if (isNetherView) wp.mcZ / 8f else wp.mcZ.toFloat()
                val screenX = centerOriginX + targetX * activeScale
                val screenY = centerOriginY + targetZ * activeScale

                if (screenX >= -100 && screenX <= canvasW + 100 && screenY >= -100 && screenY <= canvasH + 100) {
                    drawWaypointMarker(
                        screenX = screenX,
                        screenY = screenY,
                        waypoint = wp,
                        isNether = isNetherView
                    )
                }
            }

            // 4. Draw Other Members
            members.forEach { member ->
                val targetX = if (isNetherView) member.mcX / 8f else member.mcX.toFloat()
                val targetZ = if (isNetherView) member.mcZ / 8f else member.mcZ.toFloat()
                val screenX = centerOriginX + targetX * activeScale
                val screenY = centerOriginY + targetZ * activeScale

                if (screenX >= -100 && screenX <= canvasW + 100 && screenY >= -100 && screenY <= canvasH + 100) {
                    drawPlayerMarker(
                        screenX = screenX,
                        screenY = screenY,
                        gamertag = member.gamertag,
                        skinPreset = member.skinPreset,
                        heading = member.heading,
                        status = member.status,
                        isCurrentUser = false
                    )
                }
            }

            // 5. Draw Current User Marker
            val userTargetX = if (isNetherView) currentUserCoords.x / 8f else currentUserCoords.x.toFloat()
            val userTargetZ = if (isNetherView) currentUserCoords.z / 8f else currentUserCoords.z.toFloat()
            val userScreenX = centerOriginX + userTargetX * activeScale
            val userScreenY = centerOriginY + userTargetZ * activeScale

            drawPlayerMarker(
                screenX = userScreenX,
                screenY = userScreenY,
                gamertag = "${userProfile.gamertag} (Tú)",
                skinPreset = userProfile.skinPreset,
                heading = 0f,
                status = "Aquí",
                isCurrentUser = true
            )

            // 6. Draw Selected Inspected Point if any
            selectedPointCoords?.let { selected ->
                val selTargetX = if (isNetherView) selected.x / 8f else selected.x.toFloat()
                val selTargetZ = if (isNetherView) selected.z / 8f else selected.z.toFloat()
                val selScreenX = centerOriginX + selTargetX * activeScale
                val selScreenY = centerOriginY + selTargetZ * activeScale

                drawCircle(
                    color = GoldAccent,
                    radius = 16f,
                    center = Offset(selScreenX, selScreenY),
                    style = Stroke(width = 3f)
                )
                drawLine(
                    color = GoldAccent,
                    start = Offset(selScreenX - 22f, selScreenY),
                    end = Offset(selScreenX + 22f, selScreenY),
                    strokeWidth = 2f
                )
                drawLine(
                    color = GoldAccent,
                    start = Offset(selScreenX, selScreenY - 22f),
                    end = Offset(selScreenX, selScreenY + 22f),
                    strokeWidth = 2f
                )
            }
        }

        // Overlay: Minecraft Compass & Dimension Badge (Top Left / Right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Dimension Badge
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isNetherView) NetherPortalPurple.copy(alpha = 0.9f) else Color(0xDD1E252F),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isNetherView) Color(0xFFE28CFF) else DiamondCyan),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isNetherView) "🔥 DIMENSIÓN NETHER (1:8)" else "🌍 OVERWORLD (1:1)",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Compass Rose
            MinecraftCompassRose()
        }

        // Floating Control Buttons (Bottom Right)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Nether Toggle
            SmallFloatingActionButton(
                onClick = { isNetherView = !isNetherView },
                containerColor = if (isNetherView) NetherPortalPurple else Color(0xFF28303C),
                contentColor = Color.White,
                modifier = Modifier.testTag("btn_toggle_nether")
            ) {
                Icon(
                    imageVector = Icons.Default.Whatshot,
                    contentDescription = "Alternar Nether",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Zoom In
            SmallFloatingActionButton(
                onClick = { pixelsPerBlock = (pixelsPerBlock * 1.3f).coerceAtMost(5.0f) },
                containerColor = Color(0xFF28303C),
                contentColor = Color.White,
                modifier = Modifier.testTag("btn_zoom_in")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Acercar", modifier = Modifier.size(20.dp))
            }

            // Zoom Out
            SmallFloatingActionButton(
                onClick = { pixelsPerBlock = (pixelsPerBlock / 1.3f).coerceAtLeast(0.15f) },
                containerColor = Color(0xFF28303C),
                contentColor = Color.White,
                modifier = Modifier.testTag("btn_zoom_out")
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Alejar", modifier = Modifier.size(20.dp))
            }

            // Center on Spawn (0, 0)
            FloatingActionButton(
                onClick = {
                    panOffsetX = 0f
                    panOffsetY = 0f
                },
                containerColor = GoldAccent,
                contentColor = Color(0xFF1E242D),
                modifier = Modifier
                    .size(46.dp)
                    .testTag("btn_center_spawn")
            ) {
                Icon(Icons.Default.CenterFocusStrong, contentDescription = "Centrar Spawn")
            }

            // Center on Player
            FloatingActionButton(
                onClick = {
                    panOffsetX = -currentUserCoords.x * activeScale
                    panOffsetY = -currentUserCoords.z * activeScale
                },
                containerColor = GrassGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .size(46.dp)
                    .testTag("btn_center_user")
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Mi Ubicación")
            }
        }

        // Tap inspection popup card
        selectedPointCoords?.let { pt ->
            val dist = CoordinateConverter.distanceInBlocks(pt.x, pt.z).toInt()
            val biome = CoordinateConverter.estimateBiome(pt)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .fillMaxWidth(0.85f)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xF0181E27),
                    border = androidx.compose.foundation.BorderStroke(2.dp, GoldAccent),
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📍 BLOQUE INSPECCIONADO",
                                color = GoldAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            IconButton(
                                onClick = { selectedPointCoords = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text("✕", color = Color(0xFFA0AEC0), fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "X: ${pt.x} | Y: 64 | Z: ${pt.z}",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Distancia a Spawn: $dist bloques • Bioma: $biome",
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        MinecraftButton(
                            text = "Fijar Pin Aquí",
                            onClick = {
                                onAddWaypointAtCoords(pt)
                                selectedPointCoords = null
                            },
                            icon = { Icon(Icons.Default.AddLocation, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawMinecraftGrid(
    originX: Float,
    originY: Float,
    canvasWidth: Float,
    canvasHeight: Float,
    scale: Float,
    isNether: Boolean
) {
    val chunkPixels = 16f * scale
    val gridColor = if (isNether) Color(0x2B9B59B6) else Color(0x1F47A036)
    val chunkBorderColor = if (isNether) Color(0x40E74C3C) else Color(0x3B4AEDD7)

    // Calculate visible range of blocks
    val minBlockX = ((-originX) / scale).toInt() - 32
    val maxBlockX = ((canvasWidth - originX) / scale).toInt() + 32
    val minBlockZ = ((-originY) / scale).toInt() - 32
    val maxBlockZ = ((canvasHeight - originY) / scale).toInt() + 32

    // Chunks boundaries (every 16 blocks)
    val startChunkX = (minBlockX / 16) * 16
    val endChunkX = (maxBlockX / 16 + 1) * 16
    val startChunkZ = (minBlockZ / 16) * 16
    val endChunkZ = (maxBlockZ / 16 + 1) * 16

    for (bx in startChunkX..endChunkX step 16) {
        val x = originX + bx * scale
        if (x in -50f..(canvasWidth + 50f)) {
            drawLine(
                color = chunkBorderColor,
                start = Offset(x, 0f),
                end = Offset(x, canvasHeight),
                strokeWidth = 1f
            )
        }
    }

    for (bz in startChunkZ..endChunkZ step 16) {
        val y = originY + bz * scale
        if (y in -50f..(canvasHeight + 50f)) {
            drawLine(
                color = chunkBorderColor,
                start = Offset(0f, y),
                end = Offset(canvasWidth, y),
                strokeWidth = 1f
            )
        }
    }

    // Main Axes: X Axis (Redstone / Red line) and Z Axis (Diamond / Cyan line)
    if (originY in 0f..canvasHeight) {
        drawLine(
            color = RedstoneAccent.copy(alpha = 0.8f),
            start = Offset(0f, originY),
            end = Offset(canvasWidth, originY),
            strokeWidth = 2.5f
        )
    }
    if (originX in 0f..canvasWidth) {
        drawLine(
            color = DiamondCyan.copy(alpha = 0.8f),
            start = Offset(originX, 0f),
            end = Offset(originX, canvasHeight),
            strokeWidth = 2.5f
        )
    }

    // Coordinate markers on axes
    val textPaint = Paint().apply {
        color = android.graphics.Color.argb(160, 200, 210, 220)
        textSize = 24f
        typeface = Typeface.MONOSPACE
    }

    val stepBlocks = if (scale > 1.5f) 50 else if (scale > 0.5f) 100 else 250
    val startMarkX = (minBlockX / stepBlocks) * stepBlocks
    val endMarkX = (maxBlockX / stepBlocks + 1) * stepBlocks

    for (bx in startMarkX..endMarkX step stepBlocks) {
        if (bx == 0) continue
        val x = originX + bx * scale
        if (x in 20f..(canvasWidth - 20f)) {
            val labelY = originY.coerceIn(40f, canvasHeight - 20f)
            drawContext.canvas.nativeCanvas.drawText("X:$bx", x - 25f, labelY - 8f, textPaint)
        }
    }
}

private fun DrawScope.drawWorldSpawnBeacon(
    screenX: Float,
    screenY: Float,
    isNether: Boolean
) {
    // Radiating beacon rings
    drawCircle(
        color = GoldAccent.copy(alpha = 0.2f),
        radius = 28f,
        center = Offset(screenX, screenY)
    )
    drawCircle(
        color = GoldAccent.copy(alpha = 0.5f),
        radius = 18f,
        center = Offset(screenX, screenY),
        style = Stroke(width = 2.5f)
    )

    // Beacon base square
    drawRect(
        color = Color(0xFF1E2836),
        topLeft = Offset(screenX - 10f, screenY - 10f),
        size = Size(20f, 20f)
    )
    drawRect(
        color = GoldAccent,
        topLeft = Offset(screenX - 7f, screenY - 7f),
        size = Size(14f, 14f)
    )

    // Glowing beam core
    drawCircle(
        color = Color.White,
        radius = 4f,
        center = Offset(screenX, screenY)
    )

    // Text label
    val textPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 26f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
    }
    drawContext.canvas.nativeCanvas.drawText("★ SPAWN (0, 64, 0)", screenX - 110f, screenY + 34f, textPaint)
}

private fun DrawScope.drawWaypointMarker(
    screenX: Float,
    screenY: Float,
    waypoint: Waypoint,
    isNether: Boolean
) {
    val color = waypoint.type.getColor()

    // Outer glow & pin head
    drawCircle(
        color = color.copy(alpha = 0.25f),
        radius = 16f,
        center = Offset(screenX, screenY)
    )
    drawCircle(
        color = color,
        radius = 9f,
        center = Offset(screenX, screenY)
    )
    drawCircle(
        color = Color.White,
        radius = 4f,
        center = Offset(screenX, screenY)
    )

    // Title label & coords
    val titlePaint = Paint().apply {
        this.color = android.graphics.Color.WHITE
        textSize = 24f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        setShadowLayer(3f, 0f, 0f, android.graphics.Color.BLACK)
    }
    val coordsPaint = Paint().apply {
        this.color = android.graphics.Color.argb(220, 200, 210, 220)
        textSize = 19f
        typeface = Typeface.MONOSPACE
        setShadowLayer(3f, 0f, 0f, android.graphics.Color.BLACK)
    }

    val label = "${waypoint.type.iconEmoji} ${waypoint.title}"
    val coordLabel = "[X:${waypoint.mcX}, Z:${waypoint.mcZ}]"
    drawContext.canvas.nativeCanvas.drawText(label, screenX + 14f, screenY - 4f, titlePaint)
    drawContext.canvas.nativeCanvas.drawText(coordLabel, screenX + 14f, screenY + 18f, coordsPaint)
}

private fun DrawScope.drawPlayerMarker(
    screenX: Float,
    screenY: Float,
    gamertag: String,
    skinPreset: String,
    heading: Float,
    status: String,
    isCurrentUser: Boolean
) {
    val headSize = 22f
    val halfHead = headSize / 2f

    // Direction arrow pointer if rotating
    val angleRad = Math.toRadians((heading - 90).toDouble())
    val dirX = screenX + (cos(angleRad) * 20f).toFloat()
    val dirY = screenY + (sin(angleRad) * 20f).toFloat()

    val path = Path().apply {
        moveTo(dirX, dirY)
        lineTo(screenX + 8f, screenY)
        lineTo(screenX - 8f, screenY)
        close()
    }
    drawPath(
        path = path,
        color = if (isCurrentUser) GrassGreenPrimary else DiamondCyan
    )

    // Player head background & border
    drawRect(
        color = Color(0xFF1E242E),
        topLeft = Offset(screenX - halfHead - 2f, screenY - halfHead - 2f),
        size = Size(headSize + 4f, headSize + 4f)
    )

    val faceColor = when (skinPreset.lowercase()) {
        "alex" -> Color(0xFFF0BD96)
        "diamond_knight" -> Color(0xFF4AEDD7)
        "creeper" -> Color(0xFF55A33D)
        "enderman" -> Color(0xFF1B1B22)
        else -> Color(0xFFBE8B68) // Steve
    }

    drawRect(
        color = faceColor,
        topLeft = Offset(screenX - halfHead, screenY - halfHead),
        size = Size(headSize, headSize)
    )

    // Tiny eyes
    val eyeColor = if (skinPreset == "enderman") Color(0xFFB549E0) else Color(0xFF2C4482)
    drawRect(
        color = eyeColor,
        topLeft = Offset(screenX - halfHead + 3f, screenY - 2f),
        size = Size(4f, 4f)
    )
    drawRect(
        color = eyeColor,
        topLeft = Offset(screenX + halfHead - 7f, screenY - 2f),
        size = Size(4f, 4f)
    )

    // Current user highlight halo
    if (isCurrentUser) {
        drawCircle(
            color = GrassGreenPrimary,
            radius = 20f,
            center = Offset(screenX, screenY),
            style = Stroke(width = 2.5f)
        )
    }

    // Name badge above player
    val tagPaint = Paint().apply {
        color = if (isCurrentUser) android.graphics.Color.GREEN else android.graphics.Color.WHITE
        textSize = 22f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
    }
    val statusPaint = Paint().apply {
        color = android.graphics.Color.argb(190, 200, 210, 225)
        textSize = 18f
        typeface = Typeface.MONOSPACE
        setShadowLayer(3f, 0f, 0f, android.graphics.Color.BLACK)
    }

    val textX = screenX - (tagPaint.measureText(gamertag) / 2f)
    drawContext.canvas.nativeCanvas.drawText(gamertag, textX, screenY - halfHead - 8f, tagPaint)
    if (status.isNotBlank()) {
        val statusX = screenX - (statusPaint.measureText("[$status]") / 2f)
        drawContext.canvas.nativeCanvas.drawText("[$status]", statusX, screenY + halfHead + 22f, statusPaint)
    }
}

@Composable
fun MinecraftCompassRose(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(2.dp, Color(0xFF333E4D), RoundedCornerShape(6.dp))
            .background(Color(0xE6141920), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "▲ N (-Z)",
                color = RedstoneAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "◀ O (-X)",
                    color = Color(0xFFA0AEC0),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "E (+X) ▶",
                    color = GoldAccent,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = "▼ S (+Z)",
                color = DiamondCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
