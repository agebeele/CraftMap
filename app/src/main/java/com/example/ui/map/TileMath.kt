package com.example.ui.map

import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.tan

enum class MapTileLayer(
    val title: String,
    val iconEmoji: String,
    val maxZoom: Int = 18
) {
    SATELLITE("Satélite", "🛰️", 18),
    STREETS("Calles", "🗺️", 19),
    DARKSlate("Deepslate", "🌑", 19);

    fun getTileUrl(x: Int, y: Int, z: Int): String {
        return when (this) {
            SATELLITE -> "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/$z/$y/$x"
            STREETS -> "https://tile.openstreetmap.org/$z/$x/$y.png"
            DARKSlate -> "https://basemaps.cartocdn.com/dark_all/$z/$x/$y.png"
        }
    }
}

object TileMath {
    const val TILE_SIZE = 256

    /**
     * Converts longitude to tile X coordinate (floating point)
     */
    fun lngToTileX(lng: Double, zoom: Int): Double {
        return (lng + 180.0) / 360.0 * (1 shl zoom)
    }

    /**
     * Converts latitude to tile Y coordinate (floating point)
     */
    fun latToTileY(lat: Double, zoom: Int): Double {
        val latRad = Math.toRadians(lat.coerceIn(-85.0511, 85.0511))
        return (1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * (1 shl zoom)
    }

    /**
     * Converts tile X coordinate to longitude
     */
    fun tileXToLng(x: Double, zoom: Int): Double {
        return x / (1 shl zoom) * 360.0 - 180.0
    }

    /**
     * Converts tile Y coordinate to latitude
     */
    fun tileYToLat(y: Double, zoom: Int): Double {
        val n = PI - 2.0 * PI * y / (1 shl zoom)
        return Math.toDegrees(atan(sinh(n)))
    }
}
