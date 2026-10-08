package com.example.ui.map

import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sinh
import kotlin.math.tan

enum class MapTileLayer(
    val title: String,
    val iconEmoji: String,
    val maxZoom: Int = 19
) {
    GOOGLE_HYBRID("Híbrido", "🛰️", 20),
    GOOGLE_STREETS("Calles", "🗺️", 20),
    VOYAGER("Atlas", "🧭", 19),
    DEEPSLATE("Deepslate", "🌑", 19);

    fun getTileUrl(x: Int, y: Int, z: Int): String {
        return when (this) {
            GOOGLE_HYBRID -> "https://mt1.google.com/vt/lyrs=y&x=$x&y=$y&z=$z"
            GOOGLE_STREETS -> "https://mt1.google.com/vt/lyrs=m&x=$x&y=$y&z=$z"
            VOYAGER -> "https://basemaps.cartocdn.com/rastertiles/voyager/$z/$x/$y.png"
            DEEPSLATE -> "https://basemaps.cartocdn.com/dark_all/$z/$x/$y.png"
        }
    }
}

object TileMath {
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
