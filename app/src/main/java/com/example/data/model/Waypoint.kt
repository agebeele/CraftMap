package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class WaypointType(
    val labelEs: String,
    val iconEmoji: String,
    val colorHex: Long
) {
    SPAWN("World Spawn", "🌟", 0xFFF1C40F),
    HOME("Casa / Base", "🏠", 0xFF2ECC71),
    MINE("Mina / Cueva", "⛏️", 0xFF3498DB),
    NETHER_PORTAL("Portal Nether", "🟣", 0xFF9B59B6),
    VILLAGE("Aldea / Comercio", "🌾", 0xFFE67E22),
    FORTRESS("Fortaleza / Castillo", "🏰", 0xFFE74C3C),
    CUSTOM("Punto de Interés", "📍", 0xFF1ABC9C);

    fun getColor(): Color = Color(colorHex)
}

data class Waypoint(
    val id: String,
    val groupId: String,
    val title: String,
    val description: String = "",
    val type: WaypointType,
    val gpsLat: Double,
    val gpsLng: Double,
    val gpsAlt: Double = 0.0,
    val mcX: Int,
    val mcY: Int,
    val mcZ: Int,
    val creatorGamertag: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    val coords: MinecraftCoords get() = MinecraftCoords(mcX, mcY, mcZ)
}
