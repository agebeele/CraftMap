package com.example.ui.map

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.converter.CoordinateConverter
import com.example.data.model.GroupMember
import com.example.data.model.MinecraftCoords
import com.example.data.model.UserProfile
import com.example.data.model.Waypoint
import com.example.ui.components.MinecraftButton
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GrassGreenPrimary
import com.example.ui.theme.RedstoneAccent
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun RealWorldMapView(
    centerLat: Double,
    centerLng: Double,
    spawnLat: Double,
    spawnLng: Double,
    spawnAlt: Double,
    userGpsLat: Double,
    userGpsLng: Double,
    userProfile: UserProfile,
    currentUserCoords: MinecraftCoords,
    members: List<GroupMember>,
    waypoints: List<Waypoint>,
    onAddWaypointAtCoords: (MinecraftCoords, Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    // Proper tile dimension in DP so it matches exactly 256 physical pixels on any display density
    val tileSizeDp = with(density) { TileMath.TILE_SIZE.toDp() }

    // Camera Center (Lat / Lng) and Zoom Level (12 to 18)
    var cameraLat by remember(centerLat) { mutableDoubleStateOf(centerLat) }
    var cameraLng by remember(centerLng) { mutableDoubleStateOf(centerLng) }
    var zoomLevel by remember { mutableIntStateOf(16) } // Street-level zoom
    var zoomAccumulator by remember { mutableFloatStateOf(1.0f) }

    var selectedLayer by remember { mutableStateOf(MapTileLayer.SATELLITE) }
    var showChunkGrid by remember { mutableStateOf(true) }

    // Inspected Tap Point
    var inspectedLocation by remember { mutableStateOf<Triple<Double, Double, MinecraftCoords>?>(null) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F141A))
            .testTag("real_world_map_container")
    ) {
        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()
        val halfW = screenWidthPx / 2f
        val halfH = screenHeightPx / 2f

        // Center Tile Coordinates in Web Mercator
        val centerTileX = TileMath.lngToTileX(cameraLng, zoomLevel)
        val centerTileY = TileMath.latToTileY(cameraLat, zoomLevel)

        // Calculate visible tile ranges with generous margins for smooth panning
        val tilesAcross = (screenWidthPx / TileMath.TILE_SIZE).toInt() + 3
        val tilesDown = (screenHeightPx / TileMath.TILE_SIZE).toInt() + 3

        val startTileX = floor(centerTileX - tilesAcross / 2.0).toInt()
        val endTileX = startTileX + tilesAcross + 1
        val startTileY = floor(centerTileY - tilesDown / 2.0).toInt()
        val endTileY = startTileY + tilesDown + 1
        val maxTileIndex = (1 shl zoomLevel) - 1

        // 1. Gesture Detection Layer (Never cancelled on drag because key is Unit!)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        // Smooth Pan
                        val deltaTileX = pan.x / TileMath.TILE_SIZE.toDouble()
                        val deltaTileY = pan.y / TileMath.TILE_SIZE.toDouble()

                        val currentCenterTileX = TileMath.lngToTileX(cameraLng, zoomLevel)
                        val currentCenterTileY = TileMath.latToTileY(cameraLat, zoomLevel)

                        cameraLng = TileMath.tileXToLng(currentCenterTileX - deltaTileX, zoomLevel)
                        cameraLat = TileMath.tileYToLat(currentCenterTileY - deltaTileY, zoomLevel)

                        // Continuous Pinch Zoom
                        zoomAccumulator *= zoom
                        if (zoomAccumulator > 1.35f && zoomLevel < 18) {
                            zoomLevel++
                            zoomAccumulator = 1.0f
                        } else if (zoomAccumulator < 0.74f && zoomLevel > 11) {
                            zoomLevel--
                            zoomAccumulator = 1.0f
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (zoomLevel < 18) zoomLevel++
                        },
                        onTap = { tapOffset ->
                            val dxPx = tapOffset.x - halfW
                            val dyPx = tapOffset.y - halfH

                            val curCenterTileX = TileMath.lngToTileX(cameraLng, zoomLevel)
                            val curCenterTileY = TileMath.latToTileY(cameraLat, zoomLevel)

                            val tapTileX = curCenterTileX + dxPx / TileMath.TILE_SIZE.toDouble()
                            val tapTileY = curCenterTileY + dyPx / TileMath.TILE_SIZE.toDouble()

                            val tapLng = TileMath.tileXToLng(tapTileX, zoomLevel)
                            val tapLat = TileMath.tileYToLat(tapTileY, zoomLevel)

                            val mcCoords = CoordinateConverter.gpsToMinecraft(
                                targetLat = tapLat,
                                targetLng = tapLng,
                                targetAlt = spawnAlt,
                                spawnLat = spawnLat,
                                spawnLng = spawnLng,
                                spawnAlt = spawnAlt
                            )
                            inspectedLocation = Triple(tapLat, tapLng, mcCoords)
                        }
                    )
                }
        ) {
            // Render Real Map Tiles perfectly stitched without gaps or overlaps
            for (tx in startTileX..endTileX) {
                for (ty in startTileY..endTileY) {
                    if (tx in 0..maxTileIndex && ty in 0..maxTileIndex) {
                        val tileScreenXPx = halfW + ((tx - centerTileX) * TileMath.TILE_SIZE).toFloat()
                        val tileScreenYPx = halfH + ((ty - centerTileY) * TileMath.TILE_SIZE).toFloat()

                        val tileUrl = selectedLayer.getTileUrl(tx, ty, zoomLevel)

                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(tileUrl)
                                .addHeader("User-Agent", "CraftMap-Android/1.0 (Minecraft GPS Real Maps)")
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .crossfade(false)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .size(tileSizeDp)
                                .offset {
                                    IntOffset(tileScreenXPx.roundToInt(), tileScreenYPx.roundToInt())
                                }
                        )
                    }
                }
            }
        }

        // 2. Minecraft Overlay Canvas (Chunks, Spawn, Waypoints, Players)
        Canvas(modifier = Modifier.fillMaxSize()) {
            fun gpsToScreen(lat: Double, lng: Double): Offset {
                val tx = TileMath.lngToTileX(lng, zoomLevel)
                val ty = TileMath.latToTileY(lat, zoomLevel)
                val sx = halfW + ((tx - centerTileX) * TileMath.TILE_SIZE).toFloat()
                val sy = halfH + ((ty - centerTileY) * TileMath.TILE_SIZE).toFloat()
                return Offset(sx, sy)
            }

            // A. Optional Minecraft Chunk Grid (16x16m blocks)
            if (showChunkGrid && zoomLevel >= 15) {
                drawMinecraftRealChunkGrid(
                    spawnLat = spawnLat,
                    spawnLng = spawnLng,
                    zoom = zoomLevel,
                    halfW = halfW,
                    halfH = halfH,
                    centerTileX = centerTileX,
                    centerTileY = centerTileY,
                    canvasW = screenWidthPx,
                    canvasH = screenHeightPx
                )
            }

            // B. Draw World Spawn Beacon at (0, 64, 0)
            val spawnOffset = gpsToScreen(spawnLat, spawnLng)
            drawWorldSpawnBeaconMarker(spawnOffset.x, spawnOffset.y)

            // C. Draw Waypoint Pins (Homes, Mines, Portals)
            waypoints.forEach { wp ->
                val wpOffset = gpsToScreen(wp.gpsLat, wp.gpsLng)
                if (wpOffset.x in -150f..(screenWidthPx + 150f) && wpOffset.y in -150f..(screenHeightPx + 150f)) {
                    drawRealMapWaypoint(
                        screenX = wpOffset.x,
                        screenY = wpOffset.y,
                        waypoint = wp
                    )
                }
            }

            // D. Draw Other Companions
            members.forEach { m ->
                val mOffset = gpsToScreen(m.lat, m.lng)
                if (mOffset.x in -150f..(screenWidthPx + 150f) && mOffset.y in -150f..(screenHeightPx + 150f)) {
                    drawRealMapPlayerMarker(
                        screenX = mOffset.x,
                        screenY = mOffset.y,
                        gamertag = m.gamertag,
                        skinPreset = m.skinPreset,
                        heading = m.heading,
                        status = m.status,
                        isCurrentUser = false
                    )
                }
            }

            // E. Draw Current User
            val userOffset = gpsToScreen(userGpsLat, userGpsLng)
            drawRealMapPlayerMarker(
                screenX = userOffset.x,
                screenY = userOffset.y,
                gamertag = "${userProfile.gamertag} (Tú)",
                skinPreset = userProfile.skinPreset,
                heading = 0f,
                status = "Aquí",
                isCurrentUser = true
            )

            // F. Inspected point crosshair
            inspectedLocation?.let { (lat, lng, _) ->
                val ptOffset = gpsToScreen(lat, lng)
                drawCircle(
                    color = GoldAccent,
                    radius = 20f,
                    center = ptOffset,
                    style = Stroke(width = 3.5f)
                )
                drawLine(
                    color = GoldAccent,
                    start = Offset(ptOffset.x - 28f, ptOffset.y),
                    end = Offset(ptOffset.x + 28f, ptOffset.y),
                    strokeWidth = 3f
                )
                drawLine(
                    color = GoldAccent,
                    start = Offset(ptOffset.x, ptOffset.y - 28f),
                    end = Offset(ptOffset.x, ptOffset.y + 28f),
                    strokeWidth = 3f
                )
            }
        }

        // Top Layer Control Pill: Map Type Switcher
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MapTileLayer.values().forEach { layer ->
                val isSelected = selectedLayer == layer
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) GrassGreenPrimary else Color(0xDD1B232D),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) GoldAccent else Color(0xFF384556)
                    ),
                    modifier = Modifier
                        .clickable { selectedLayer = layer }
                        .testTag("layer_btn_${layer.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(layer.iconEmoji, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = layer.title,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Chunk Grid Toggle Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (showChunkGrid) GoldAccent.copy(alpha = 0.3f) else Color(0xDD1B232D),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (showChunkGrid) GoldAccent else Color(0xFF384556)
                ),
                modifier = Modifier.clickable { showChunkGrid = !showChunkGrid }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.GridOn,
                        contentDescription = "Cuadrícula Vóxel",
                        tint = if (showChunkGrid) GoldAccent else Color(0xFFA0AEC0),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Vóxels",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Floating Action Buttons (Zoom & Center Controls)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Zoom In (+)
            SmallFloatingActionButton(
                onClick = { if (zoomLevel < 18) zoomLevel++ },
                containerColor = Color(0xFF1F2937),
                contentColor = Color.White,
                modifier = Modifier.testTag("btn_map_zoom_in")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Acercar", modifier = Modifier.size(20.dp))
            }

            // Zoom Out (-)
            SmallFloatingActionButton(
                onClick = { if (zoomLevel > 11) zoomLevel-- },
                containerColor = Color(0xFF1F2937),
                contentColor = Color.White,
                modifier = Modifier.testTag("btn_map_zoom_out")
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Alejar", modifier = Modifier.size(20.dp))
            }

            // Center on World Spawn (0, 64, 0)
            FloatingActionButton(
                onClick = {
                    cameraLat = spawnLat
                    cameraLng = spawnLng
                    zoomLevel = 16
                },
                containerColor = GoldAccent,
                contentColor = Color(0xFF1A222C),
                modifier = Modifier.size(46.dp).testTag("btn_center_spawn_real")
            ) {
                Icon(Icons.Default.CenterFocusStrong, contentDescription = "Centrar en World Spawn")
            }

            // Center on My Location
            FloatingActionButton(
                onClick = {
                    cameraLat = userGpsLat
                    cameraLng = userGpsLng
                    zoomLevel = 16
                },
                containerColor = GrassGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.size(46.dp).testTag("btn_center_user_real")
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Mi Ubicación")
            }
        }

        // Inspected Point Info Box
        inspectedLocation?.let { (tapLat, tapLng, coords) ->
            val dist = CoordinateConverter.distanceInBlocks(coords.x, coords.z).toInt()
            val dir = CoordinateConverter.compassDirection(coords.x, coords.z)
            val biome = CoordinateConverter.estimateBiome(coords)

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
                    .fillMaxWidth(0.85f)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xF2161E28),
                    border = androidx.compose.foundation.BorderStroke(2.dp, GoldAccent),
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📍 BLOQUE EN EL MUNDO REAL",
                                color = GoldAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            IconButton(
                                onClick = { inspectedLocation = null },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Text("✕", color = Color(0xFFA0AEC0), fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bloques: [X: ${coords.x}, Y: 64, Z: ${coords.z}]",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "🌟 A $dist bloques de distancia ($dir del Spawn)",
                            color = DiamondCyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "GPS: ${String.format("%.6f", tapLat)}, ${String.format("%.6f", tapLng)} • Bioma: $biome",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        MinecraftButton(
                            text = "Fijar Pin Aquí (Casa/Mina)",
                            onClick = {
                                onAddWaypointAtCoords(coords, tapLat, tapLng)
                                inspectedLocation = null
                            },
                            icon = { Icon(Icons.Default.AddLocation, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawMinecraftRealChunkGrid(
    spawnLat: Double,
    spawnLng: Double,
    zoom: Int,
    halfW: Float,
    halfH: Float,
    centerTileX: Double,
    centerTileY: Double,
    canvasW: Float,
    canvasH: Float
) {
    // In Minecraft, 1 chunk = 16 blocks (16 meters)
    val latRad = Math.toRadians(spawnLat)
    val metersPerDegreeLat = 111195.0
    val metersPerDegreeLng = 111195.0 * cos(latRad).coerceAtLeast(0.01)

    // 16 meters in degrees:
    val chunkDLat = 16.0 / metersPerDegreeLat
    val chunkDLng = 16.0 / metersPerDegreeLng

    val chunkTileW = (chunkDLng / 360.0 * (1 shl zoom)) * TileMath.TILE_SIZE
    val chunkTileH = (chunkDLat / 360.0 * (1 shl zoom)) * TileMath.TILE_SIZE

    if (chunkTileW < 10f) return // Too small to draw cleanly

    // Project Spawn onto screen
    val spawnTileX = TileMath.lngToTileX(spawnLng, zoom)
    val spawnTileY = TileMath.latToTileY(spawnLat, zoom)
    val spawnScreenX = halfW + ((spawnTileX - centerTileX) * TileMath.TILE_SIZE).toFloat()
    val spawnScreenY = halfH + ((spawnTileY - centerTileY) * TileMath.TILE_SIZE).toFloat()

    val gridColor = Color(0x4047A036) // Translucent Minecraft Emerald grid

    // Draw vertical chunk grid lines
    var x = spawnScreenX % chunkTileW.toFloat()
    if (x < 0) x += chunkTileW.toFloat()
    while (x < canvasW) {
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, canvasH),
            strokeWidth = 1.2f
        )
        x += chunkTileW.toFloat()
    }

    // Draw horizontal chunk grid lines
    var y = spawnScreenY % chunkTileH.toFloat()
    if (y < 0) y += chunkTileH.toFloat()
    while (y < canvasH) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(canvasW, y),
            strokeWidth = 1.2f
        )
        y += chunkTileH.toFloat()
    }
}

private fun DrawScope.drawWorldSpawnBeaconMarker(
    screenX: Float,
    screenY: Float
) {
    // Glowing Beacon Beam & Star
    drawCircle(
        color = GoldAccent.copy(alpha = 0.25f),
        radius = 32f,
        center = Offset(screenX, screenY)
    )
    drawCircle(
        color = GoldAccent.copy(alpha = 0.6f),
        radius = 20f,
        center = Offset(screenX, screenY),
        style = Stroke(width = 3f)
    )

    // Golden Beacon Diamond
    drawRect(
        color = Color(0xFF141D26),
        topLeft = Offset(screenX - 11f, screenY - 11f),
        size = Size(22f, 22f)
    )
    drawRect(
        color = GoldAccent,
        topLeft = Offset(screenX - 8f, screenY - 8f),
        size = Size(16f, 16f)
    )
    drawCircle(
        color = Color.White,
        radius = 4f,
        center = Offset(screenX, screenY)
    )

    // Beacon label
    val textPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 28f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        setShadowLayer(5f, 0f, 0f, android.graphics.Color.BLACK)
    }
    drawContext.canvas.nativeCanvas.drawText("★ SPAWN (0, 64, 0)", screenX - 120f, screenY + 40f, textPaint)
}

private fun DrawScope.drawRealMapWaypoint(
    screenX: Float,
    screenY: Float,
    waypoint: Waypoint
) {
    val color = waypoint.type.getColor()

    // Outer glow
    drawCircle(
        color = color.copy(alpha = 0.35f),
        radius = 20f,
        center = Offset(screenX, screenY)
    )
    drawCircle(
        color = color,
        radius = 11f,
        center = Offset(screenX, screenY)
    )
    drawCircle(
        color = Color.White,
        radius = 5f,
        center = Offset(screenX, screenY)
    )

    val labelPaint = Paint().apply {
        this.color = android.graphics.Color.WHITE
        textSize = 26f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
    }
    val coordsPaint = Paint().apply {
        this.color = android.graphics.Color.argb(230, 220, 230, 240)
        textSize = 20f
        typeface = Typeface.MONOSPACE
        setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
    }

    val label = "${waypoint.type.iconEmoji} ${waypoint.title}"
    val coordLabel = "[X:${waypoint.mcX}, Z:${waypoint.mcZ}]"
    drawContext.canvas.nativeCanvas.drawText(label, screenX + 16f, screenY - 4f, labelPaint)
    drawContext.canvas.nativeCanvas.drawText(coordLabel, screenX + 16f, screenY + 22f, coordsPaint)
}

private fun DrawScope.drawRealMapPlayerMarker(
    screenX: Float,
    screenY: Float,
    gamertag: String,
    skinPreset: String,
    heading: Float,
    status: String,
    isCurrentUser: Boolean
) {
    val headSize = 26f
    val halfHead = headSize / 2f

    // Heading direction arrow
    val angleRad = Math.toRadians((heading - 90).toDouble())
    val dirX = screenX + (cos(angleRad) * 24f).toFloat()
    val dirY = screenY + (sin(angleRad) * 24f).toFloat()

    val path = Path().apply {
        moveTo(dirX, dirY)
        lineTo(screenX + 9f, screenY)
        lineTo(screenX - 9f, screenY)
        close()
    }
    drawPath(
        path = path,
        color = if (isCurrentUser) GrassGreenPrimary else DiamondCyan
    )

    // Player head background & border
    drawRect(
        color = Color(0xFF141922),
        topLeft = Offset(screenX - halfHead - 2.5f, screenY - halfHead - 2.5f),
        size = Size(headSize + 5f, headSize + 5f)
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

    // Eyes
    val eyeColor = if (skinPreset == "enderman") Color(0xFFB549E0) else Color(0xFF2C4482)
    drawRect(
        color = eyeColor,
        topLeft = Offset(screenX - halfHead + 4f, screenY - 3f),
        size = Size(5f, 5f)
    )
    drawRect(
        color = eyeColor,
        topLeft = Offset(screenX + halfHead - 9f, screenY - 3f),
        size = Size(5f, 5f)
    )

    // Current user highlight halo
    if (isCurrentUser) {
        drawCircle(
            color = GrassGreenPrimary,
            radius = 24f,
            center = Offset(screenX, screenY),
            style = Stroke(width = 3f)
        )
    }

    // Name badge above player
    val tagPaint = Paint().apply {
        color = if (isCurrentUser) android.graphics.Color.GREEN else android.graphics.Color.WHITE
        textSize = 24f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
    }
    val textX = screenX - (tagPaint.measureText(gamertag) / 2f)
    drawContext.canvas.nativeCanvas.drawText(gamertag, textX, screenY - halfHead - 10f, tagPaint)
}
