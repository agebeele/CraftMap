package com.example.ui.map

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
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
import com.example.ui.components.MapItemDetailCard
import com.example.ui.components.SelectedMapItem
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GrassGreenPrimary
import com.example.ui.theme.RedstoneAccent
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun RealWorldMapView(
    centerLat: Double,
    centerLng: Double,
    cameraTarget: Pair<Double, Double>? = null,
    spawnLat: Double,
    spawnLng: Double,
    spawnAlt: Double,
    realmName: String,
    userGpsLat: Double,
    userGpsLng: Double,
    userProfile: UserProfile,
    currentUserCoords: MinecraftCoords,
    members: List<GroupMember>,
    waypoints: List<Waypoint>,
    onAddWaypointAtCoords: (MinecraftCoords, Double, Double) -> Unit,
    onDeleteWaypoint: (String) -> Unit = {},
    onTeleportToSpawn: () -> Unit = {},
    onSelectionChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    // Standard tile display size on screen (256 dp = perfectly legible streets and labels)
    val tileDisplaySizePx = with(density) { 256.dp.toPx() }
    val tileSizeDp = 256.dp

    // Camera Center (Lat / Lng)
    var cameraLat by remember { mutableDoubleStateOf(centerLat) }
    var cameraLng by remember { mutableDoubleStateOf(centerLng) }
    var zoomLevel by remember { mutableIntStateOf(16) }
    var pinchScale by remember { mutableFloatStateOf(1.0f) }

    // When an explicit cameraTarget arrives (e.g. realm switch, GPS button, teleport), jump to it!
    LaunchedEffect(cameraTarget) {
        if (cameraTarget != null) {
            cameraLat = cameraTarget.first
            cameraLng = cameraTarget.second
        }
    }

    var selectedLayer by remember { mutableStateOf(MapTileLayer.GOOGLE_HYBRID) }
    var showChunkGrid by remember { mutableStateOf(true) }

    // Selected item for info inspection
    var selectedItem by remember { mutableStateOf<SelectedMapItem?>(null) }

    // Notify parent screen (MapScreen) so it can hide the FAB and adapt controls
    LaunchedEffect(selectedItem) {
        onSelectionChanged(selectedItem != null)
    }

    // Hardware/System Back Button dismisses any open inspector card
    BackHandler(enabled = selectedItem != null) {
        selectedItem = null
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .clipToBounds()
            .testTag("real_world_map_container")
    ) {
        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()
        val halfW = screenWidthPx / 2f
        val halfH = screenHeightPx / 2f

        val effectiveTileSize = tileDisplaySizePx * pinchScale

        // Center Tile Coordinates in Web Mercator
        val centerTileX = TileMath.lngToTileX(cameraLng, zoomLevel)
        val centerTileY = TileMath.latToTileY(cameraLat, zoomLevel)

        // Calculate tile range with exact screen boundaries + 1 safety margin tile
        val tilesAcross = ceil(screenWidthPx / effectiveTileSize).toInt() + 2
        val tilesDown = ceil(screenHeightPx / effectiveTileSize).toInt() + 2

        val startTileX = floor(centerTileX - tilesAcross / 2.0).toInt()
        val endTileX = startTileX + tilesAcross
        val startTileY = floor(centerTileY - tilesDown / 2.0).toInt()
        val endTileY = startTileY + tilesDown
        val maxTileIndex = (1 shl zoomLevel) - 1

        // Helper to project GPS lat/lng onto current screen offset
        fun gpsToScreen(lat: Double, lng: Double): Offset {
            val tx = TileMath.lngToTileX(lng, zoomLevel)
            val ty = TileMath.latToTileY(lat, zoomLevel)
            val sx = halfW + ((tx - centerTileX) * effectiveTileSize).toFloat()
            val sy = halfH + ((ty - centerTileY) * effectiveTileSize).toFloat()
            return Offset(sx, sy)
        }

        // Tap hit-testing function
        fun handleMapTap(tapOffset: Offset) {
            val touchRadiusPx = with(density) { 40.dp.toPx() } // Generous 40dp radius for easy touch

            // 1. Waypoints
            val hitWaypoint = waypoints.firstOrNull { wp ->
                val pos = gpsToScreen(wp.gpsLat, wp.gpsLng)
                val dx = tapOffset.x - pos.x
                val dy = tapOffset.y - pos.y
                sqrt(dx * dx + dy * dy) <= touchRadiusPx
            }

            // 2. World Spawn Beacon
            val spawnPos = gpsToScreen(spawnLat, spawnLng)
            val distSpawn = sqrt((tapOffset.x - spawnPos.x) * (tapOffset.x - spawnPos.x) + (tapOffset.y - spawnPos.y) * (tapOffset.y - spawnPos.y))
            val hitSpawn = distSpawn <= touchRadiusPx

            // 3. Other Companion Members
            val hitMember = members.firstOrNull { m ->
                val pos = gpsToScreen(m.lat, m.lng)
                val dx = tapOffset.x - pos.x
                val dy = tapOffset.y - pos.y
                sqrt(dx * dx + dy * dy) <= touchRadiusPx
            }

            // 4. Current User Marker
            val userPos = gpsToScreen(userGpsLat, userGpsLng)
            val distUser = sqrt((tapOffset.x - userPos.x) * (tapOffset.x - userPos.x) + (tapOffset.y - userPos.y) * (tapOffset.y - userPos.y))
            val hitUser = distUser <= touchRadiusPx

            val hitMarkerItem: SelectedMapItem? = when {
                hitWaypoint != null -> SelectedMapItem.WaypointItem(hitWaypoint)
                hitSpawn -> SelectedMapItem.SpawnItem(spawnLat, spawnLng, spawnAlt, realmName)
                hitMember != null -> SelectedMapItem.PlayerItem(hitMember)
                hitUser -> SelectedMapItem.CurrentUserItem(userProfile, currentUserCoords, userGpsLat, userGpsLng)
                else -> null
            }

            if (selectedItem != null) {
                // An info popup is currently open
                if (hitMarkerItem != null) {
                    // User tapped another explicit marker -> switch to it
                    selectedItem = hitMarkerItem
                } else {
                    // User tapped anywhere else on the map / ground -> DISMISS the popup cleanly!
                    selectedItem = null
                }
            } else {
                // No popup open currently
                if (hitMarkerItem != null) {
                    selectedItem = hitMarkerItem
                } else {
                    // User tapped on real-world ground: compute exact GPS and Minecraft coordinates
                    val dxPx = tapOffset.x - halfW
                    val dyPx = tapOffset.y - halfH
                    val tapTileX = centerTileX + dxPx / effectiveTileSize.toDouble()
                    val tapTileY = centerTileY + dyPx / effectiveTileSize.toDouble()
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
                    selectedItem = SelectedMapItem.InspectedBlockItem(mcCoords, tapLat, tapLng)
                }
            }
        }

        // UNIFIED GESTURE DETECTOR: Handles Pan, Pinch Zoom, Double-Tap, and Item Selection flawlessly
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(zoomLevel) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startPos = down.position
                        var isTransforming = false
                        var lastTapTime = 0L

                        do {
                            val event = awaitPointerEvent()
                            if (event.changes.any { it.isConsumed }) break

                            if (event.changes.size > 1) {
                                // Multi-finger pinch zoom & pan
                                isTransforming = true
                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()

                                val currentEffTile = tileDisplaySizePx * pinchScale
                                val deltaX = panChange.x / currentEffTile.toDouble()
                                val deltaY = panChange.y / currentEffTile.toDouble()

                                val curTileX = TileMath.lngToTileX(cameraLng, zoomLevel)
                                val curTileY = TileMath.latToTileY(cameraLat, zoomLevel)

                                cameraLng = TileMath.tileXToLng(curTileX - deltaX, zoomLevel)
                                cameraLat = TileMath.tileYToLat(curTileY - deltaY, zoomLevel)

                                var newScale = pinchScale * zoomChange
                                if (newScale > 1.35f && zoomLevel < 19) {
                                    zoomLevel++
                                    newScale /= 2.0f
                                } else if (newScale < 0.72f && zoomLevel > 11) {
                                    zoomLevel--
                                    newScale *= 2.0f
                                }
                                pinchScale = newScale.coerceIn(0.72f, 1.35f)

                                event.changes.forEach { it.consume() }
                            } else if (event.changes.size == 1) {
                                val change = event.changes.first()
                                val pan = change.positionChange()

                                if (!isTransforming) {
                                    if ((change.position - startPos).getDistance() > viewConfiguration.touchSlop) {
                                        isTransforming = true
                                    }
                                }

                                if (isTransforming) {
                                    val currentEffTile = tileDisplaySizePx * pinchScale
                                    val deltaX = pan.x / currentEffTile.toDouble()
                                    val deltaY = pan.y / currentEffTile.toDouble()

                                    val curTileX = TileMath.lngToTileX(cameraLng, zoomLevel)
                                    val curTileY = TileMath.latToTileY(cameraLat, zoomLevel)

                                    cameraLng = TileMath.tileXToLng(curTileX - deltaX, zoomLevel)
                                    cameraLat = TileMath.tileYToLat(curTileY - deltaY, zoomLevel)

                                    change.consume()
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        // Release: Normalize pinchScale back to 1.0f smoothly
                        pinchScale = 1.0f

                        if (!isTransforming) {
                            // User performed a clean TAP!
                            handleMapTap(startPos)
                        }
                    }
                }
        ) {
            // MAP TILES LAYER (Properly keyed, seamless 1px overlap, no puzzle gaps)
            for (tx in startTileX..endTileX) {
                for (ty in startTileY..endTileY) {
                    if (tx in 0..maxTileIndex && ty in 0..maxTileIndex) {
                        val tileScreenXPx = halfW + ((tx - centerTileX) * effectiveTileSize).toFloat()
                        val tileScreenYPx = halfH + ((ty - centerTileY) * effectiveTileSize).toFloat()

                        key(selectedLayer, zoomLevel, tx, ty) {
                            val tileUrl = selectedLayer.getTileUrl(tx, ty, zoomLevel)

                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(tileUrl)
                                    .addHeader("User-Agent", "CraftMap-Android/2.0")
                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .crossfade(true)
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

            // MINECRAFT OVERLAY CANVAS (Chunks, World Spawn, Waypoints, Players, Target Reticle)
            Canvas(modifier = Modifier.fillMaxSize()) {
                // A. Chunk Grid (16m block boundaries)
                if (showChunkGrid && zoomLevel >= 15) {
                    drawMinecraftRealChunkGrid(
                        spawnLat = spawnLat,
                        spawnLng = spawnLng,
                        zoom = zoomLevel,
                        halfW = halfW,
                        halfH = halfH,
                        centerTileX = centerTileX,
                        centerTileY = centerTileY,
                        effectiveTileSize = effectiveTileSize,
                        canvasW = screenWidthPx,
                        canvasH = screenHeightPx
                    )
                }

                // B. World Spawn Beacon at (0, 64, 0)
                val spawnOffset = gpsToScreen(spawnLat, spawnLng)
                drawWorldSpawnBeaconMarker(spawnOffset.x, spawnOffset.y)

                // C. Waypoint Pins (Homes, Mines, Portals, Caves)
                waypoints.forEach { wp ->
                    val wpOffset = gpsToScreen(wp.gpsLat, wp.gpsLng)
                    if (wpOffset.x in -160f..(screenWidthPx + 160f) && wpOffset.y in -160f..(screenHeightPx + 160f)) {
                        drawRealMapWaypoint(
                            screenX = wpOffset.x,
                            screenY = wpOffset.y,
                            waypoint = wp
                        )
                    }
                }

                // D. Other Companions
                members.forEach { m ->
                    val mOffset = gpsToScreen(m.lat, m.lng)
                    if (mOffset.x in -160f..(screenWidthPx + 160f) && mOffset.y in -160f..(screenHeightPx + 160f)) {
                        drawRealMapPlayerMarker(
                            screenX = mOffset.x,
                            screenY = mOffset.y,
                            gamertag = m.gamertag,
                            skinPreset = m.skinPreset,
                            heading = m.heading,
                            isCurrentUser = false
                        )
                    }
                }

                // E. Current User Avatar & Position
                val userOffset = gpsToScreen(userGpsLat, userGpsLng)
                drawRealMapPlayerMarker(
                    screenX = userOffset.x,
                    screenY = userOffset.y,
                    gamertag = "${userProfile.gamertag} (Tú)",
                    skinPreset = userProfile.skinPreset,
                    heading = 0f,
                    isCurrentUser = true
                )

                // F. Active Selected Item Reticle / Crosshair
                selectedItem?.let { item ->
                    val (tLat, tLng) = when (item) {
                        is SelectedMapItem.WaypointItem -> Pair(item.waypoint.gpsLat, item.waypoint.gpsLng)
                        is SelectedMapItem.SpawnItem -> Pair(item.lat, item.lng)
                        is SelectedMapItem.PlayerItem -> Pair(item.member.lat, item.member.lng)
                        is SelectedMapItem.CurrentUserItem -> Pair(item.lat, item.lng)
                        is SelectedMapItem.InspectedBlockItem -> Pair(item.lat, item.lng)
                    }
                    val ptOffset = gpsToScreen(tLat, tLng)

                    drawCircle(
                        color = GoldAccent.copy(alpha = 0.35f),
                        radius = 36f,
                        center = ptOffset
                    )
                    drawCircle(
                        color = GoldAccent,
                        radius = 26f,
                        center = ptOffset,
                        style = Stroke(width = 3.5f)
                    )
                    // Crosshair guides
                    drawLine(
                        color = GoldAccent,
                        start = Offset(ptOffset.x - 38f, ptOffset.y),
                        end = Offset(ptOffset.x + 38f, ptOffset.y),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = GoldAccent,
                        start = Offset(ptOffset.x, ptOffset.y - 38f),
                        end = Offset(ptOffset.x, ptOffset.y + 38f),
                        strokeWidth = 3f
                    )
                }
            }
        }

        // TOP CONTROLS: Layer Switcher & Chunk Grid Toggle
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MapTileLayer.values().forEach { layer ->
                    val isSelected = selectedLayer == layer
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) GrassGreenPrimary else Color(0xEE1A222C),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) GoldAccent else Color(0xFF384556)
                        ),
                        modifier = Modifier
                            .clickable { selectedLayer = layer }
                            .testTag("layer_btn_${layer.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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

                // Chunk Grid Toggle
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (showChunkGrid) GoldAccent.copy(alpha = 0.25f) else Color(0xEE1A222C),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (showChunkGrid) GoldAccent else Color(0xFF384556)
                    ),
                    modifier = Modifier.clickable { showChunkGrid = !showChunkGrid }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
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
                            text = "Vóxel",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // QUICK MARKERS CAROUSEL (Allows instant 1-tap jump and inspector for any pin or player!)
            if (waypoints.isNotEmpty() || members.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Spawn chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xEE151C26),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                        modifier = Modifier.clickable {
                            cameraLat = spawnLat
                            cameraLng = spawnLng
                            zoomLevel = 16
                            selectedItem = SelectedMapItem.SpawnItem(spawnLat, spawnLng, spawnAlt, realmName)
                        }
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("★ Spawn (0,64,0)", color = GoldAccent, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Waypoint chips
                    waypoints.take(6).forEach { wp ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xEE151C26),
                            border = androidx.compose.foundation.BorderStroke(1.dp, wp.type.getColor()),
                            modifier = Modifier.clickable {
                                cameraLat = wp.gpsLat
                                cameraLng = wp.gpsLng
                                zoomLevel = 17
                                selectedItem = SelectedMapItem.WaypointItem(wp)
                            }
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("${wp.type.iconEmoji} ${wp.title}", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    // Members chips
                    members.forEach { m ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xEE151C26),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DiamondCyan),
                            modifier = Modifier.clickable {
                                cameraLat = m.lat
                                cameraLng = m.lng
                                zoomLevel = 17
                                selectedItem = SelectedMapItem.PlayerItem(m)
                            }
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("👤 @${m.gamertag}", color = DiamondCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }

        // FLOATING ACTION BUTTONS (Smooth Zoom & Location Controls)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Zoom In (+)
            SmallFloatingActionButton(
                onClick = { if (zoomLevel < 19) zoomLevel++ },
                containerColor = Color(0xFF1E2633),
                contentColor = Color.White,
                modifier = Modifier.testTag("btn_map_zoom_in")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Acercar", modifier = Modifier.size(20.dp))
            }

            // Zoom Out (-)
            SmallFloatingActionButton(
                onClick = { if (zoomLevel > 11) zoomLevel-- },
                containerColor = Color(0xFF1E2633),
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
                    selectedItem = SelectedMapItem.SpawnItem(spawnLat, spawnLng, spawnAlt, realmName)
                },
                containerColor = GoldAccent,
                contentColor = Color(0xFF131820),
                modifier = Modifier.size(46.dp).testTag("btn_center_spawn_real")
            ) {
                Icon(Icons.Default.CenterFocusStrong, contentDescription = "Centrar en World Spawn")
            }

            // Center on My Location
            FloatingActionButton(
                onClick = {
                    cameraLat = userGpsLat
                    cameraLng = userGpsLng
                    zoomLevel = 17
                    selectedItem = SelectedMapItem.CurrentUserItem(userProfile, currentUserCoords, userGpsLat, userGpsLng)
                },
                containerColor = GrassGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.size(46.dp).testTag("btn_center_user_real")
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Mi Ubicación")
            }
        }

        // RICH MARKER & POINT INSPECTOR CARD (Shows detailed info when tapped!)
        AnimatedVisibility(
            visible = selectedItem != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
                .fillMaxWidth(0.94f)
        ) {
            selectedItem?.let { item ->
                MapItemDetailCard(
                    item = item,
                    currentUserCoords = currentUserCoords,
                    onDismiss = { selectedItem = null },
                    onAddPinHere = { coords, lat, lng ->
                        onAddWaypointAtCoords(coords, lat, lng)
                        selectedItem = null
                    },
                    onDeleteWaypoint = onDeleteWaypoint,
                    onTeleportToSpawn = onTeleportToSpawn
                )
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
    effectiveTileSize: Float,
    canvasW: Float,
    canvasH: Float
) {
    val latRad = Math.toRadians(spawnLat)
    val metersPerDegreeLat = 111195.0
    val metersPerDegreeLng = 111195.0 * cos(latRad).coerceAtLeast(0.01)

    // 1 chunk = 16 meters in Minecraft
    val chunkDLat = 16.0 / metersPerDegreeLat
    val chunkDLng = 16.0 / metersPerDegreeLng

    val chunkTileW = (chunkDLng / 360.0 * (1 shl zoom)) * effectiveTileSize
    val chunkTileH = (chunkDLat / 360.0 * (1 shl zoom)) * effectiveTileSize

    if (chunkTileW < 12f) return

    val spawnTileX = TileMath.lngToTileX(spawnLng, zoom)
    val spawnTileY = TileMath.latToTileY(spawnLat, zoom)
    val spawnScreenX = halfW + ((spawnTileX - centerTileX) * effectiveTileSize).toFloat()
    val spawnScreenY = halfH + ((spawnTileY - centerTileY) * effectiveTileSize).toFloat()

    val gridColor = Color(0x3547A036) // Translucent emerald voxel lines

    // Vertical grid lines
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

    // Horizontal grid lines
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
    // Outer beacon glow
    drawCircle(
        color = GoldAccent.copy(alpha = 0.25f),
        radius = 34f,
        center = Offset(screenX, screenY)
    )
    drawCircle(
        color = GoldAccent.copy(alpha = 0.7f),
        radius = 22f,
        center = Offset(screenX, screenY),
        style = Stroke(width = 3.5f)
    )

    // Golden Beacon core
    drawRect(
        color = Color(0xFF141D26),
        topLeft = Offset(screenX - 12f, screenY - 12f),
        size = Size(24f, 24f)
    )
    drawRect(
        color = GoldAccent,
        topLeft = Offset(screenX - 9f, screenY - 9f),
        size = Size(18f, 18f)
    )
    drawCircle(
        color = Color.White,
        radius = 4.5f,
        center = Offset(screenX, screenY)
    )

    // Beacon text label
    val textPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 30f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        setShadowLayer(6f, 0f, 0f, android.graphics.Color.BLACK)
    }
    drawContext.canvas.nativeCanvas.drawText("★ SPAWN [0, 64, 0]", screenX - 130f, screenY + 44f, textPaint)
}

private fun DrawScope.drawRealMapWaypoint(
    screenX: Float,
    screenY: Float,
    waypoint: Waypoint
) {
    val color = waypoint.type.getColor()

    // Outer glow & pin head
    drawCircle(
        color = color.copy(alpha = 0.35f),
        radius = 22f,
        center = Offset(screenX, screenY)
    )
    drawCircle(
        color = color,
        radius = 12f,
        center = Offset(screenX, screenY)
    )
    drawCircle(
        color = Color.White,
        radius = 5.5f,
        center = Offset(screenX, screenY)
    )

    val labelPaint = Paint().apply {
        this.color = android.graphics.Color.WHITE
        textSize = 28f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        setShadowLayer(5f, 0f, 0f, android.graphics.Color.BLACK)
    }
    val coordsPaint = Paint().apply {
        this.color = android.graphics.Color.argb(235, 220, 230, 245)
        textSize = 21f
        typeface = Typeface.MONOSPACE
        setShadowLayer(5f, 0f, 0f, android.graphics.Color.BLACK)
    }

    val label = "${waypoint.type.iconEmoji} ${waypoint.title}"
    val coordLabel = "[X:${waypoint.mcX}, Z:${waypoint.mcZ}]"
    drawContext.canvas.nativeCanvas.drawText(label, screenX + 18f, screenY - 4f, labelPaint)
    drawContext.canvas.nativeCanvas.drawText(coordLabel, screenX + 18f, screenY + 24f, coordsPaint)
}

private fun DrawScope.drawRealMapPlayerMarker(
    screenX: Float,
    screenY: Float,
    gamertag: String,
    skinPreset: String,
    heading: Float,
    isCurrentUser: Boolean
) {
    val headSize = 28f
    val halfHead = headSize / 2f

    // Heading pointer arrow
    val angleRad = Math.toRadians((heading - 90).toDouble())
    val dirX = screenX + (cos(angleRad) * 26f).toFloat()
    val dirY = screenY + (sin(angleRad) * 26f).toFloat()

    val path = Path().apply {
        moveTo(dirX, dirY)
        lineTo(screenX + 10f, screenY)
        lineTo(screenX - 10f, screenY)
        close()
    }
    drawPath(
        path = path,
        color = if (isCurrentUser) GrassGreenPrimary else DiamondCyan
    )

    // Player head background
    drawRect(
        color = Color(0xFF141922),
        topLeft = Offset(screenX - halfHead - 3f, screenY - halfHead - 3f),
        size = Size(headSize + 6f, headSize + 6f)
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
        topLeft = Offset(screenX - halfHead + 4.5f, screenY - 2f),
        size = Size(5.5f, 5.5f)
    )
    drawRect(
        color = eyeColor,
        topLeft = Offset(screenX + halfHead - 10f, screenY - 2f),
        size = Size(5.5f, 5.5f)
    )

    if (isCurrentUser) {
        drawCircle(
            color = GrassGreenPrimary,
            radius = 26f,
            center = Offset(screenX, screenY),
            style = Stroke(width = 3.5f)
        )
    }

    // Name badge
    val tagPaint = Paint().apply {
        color = if (isCurrentUser) android.graphics.Color.GREEN else android.graphics.Color.WHITE
        textSize = 26f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        setShadowLayer(5f, 0f, 0f, android.graphics.Color.BLACK)
    }
    val textX = screenX - (tagPaint.measureText(gamertag) / 2f)
    drawContext.canvas.nativeCanvas.drawText(gamertag, textX, screenY - halfHead - 12f, tagPaint)
}
