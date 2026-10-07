package com.example.data.converter

import com.example.data.model.GpsLocation
import com.example.data.model.MinecraftCoords
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt

object CoordinateConverter {
    // Earth's mean radius in meters (1 block = 1 meter in Minecraft)
    private const val EARTH_RADIUS_METERS = 6371000.0
    private const val DEFAULT_SPAWN_Y = 64

    /**
     * Converts GPS (Lat, Lng, Alt) to Minecraft block coordinates (X, Y, Z)
     * relative to a defined World Spawn GPS origin (which becomes X=0, Y=64, Z=0).
     *
     * Minecraft Axis convention:
     * - East is +X, West is -X
     * - South is +Z, North is -Z
     * - Up is +Y, Down is -Y (Sea level is 64, range: -64 to 320)
     */
    fun gpsToMinecraft(
        targetLat: Double,
        targetLng: Double,
        targetAlt: Double = 0.0,
        spawnLat: Double,
        spawnLng: Double,
        spawnAlt: Double = 0.0
    ): MinecraftCoords {
        val dLat = Math.toRadians(targetLat - spawnLat)
        val dLng = Math.toRadians(targetLng - spawnLng)
        val avgLat = Math.toRadians((targetLat + spawnLat) / 2.0)

        // Delta X (East + / West -)
        val eastWestMeters = dLng * EARTH_RADIUS_METERS * cos(avgLat)
        val mcX = eastWestMeters.roundToInt()

        // Delta Z (South + / North -)
        // Since North is higher latitude (+dLat) and Minecraft North is -Z:
        val northSouthMeters = -(dLat * EARTH_RADIUS_METERS)
        val mcZ = northSouthMeters.roundToInt()

        // Delta Y (Elevation in meters)
        val altDiff = (targetAlt - spawnAlt).roundToInt()
        val mcY = (DEFAULT_SPAWN_Y + altDiff).coerceIn(-64, 320)

        return MinecraftCoords(x = mcX, y = mcY, z = mcZ)
    }

    /**
     * Reverse converts Minecraft block coordinates (X, Y, Z) back to GPS (Lat, Lng, Alt)
     * based on World Spawn GPS origin.
     */
    fun minecraftToGps(
        mcCoords: MinecraftCoords,
        spawnLat: Double,
        spawnLng: Double,
        spawnAlt: Double = 0.0
    ): GpsLocation {
        // Delta Lat from Z: Z = - (dLat_rad * R)  =>  dLat_rad = -Z / R
        val dLatRad = -mcCoords.z.toDouble() / EARTH_RADIUS_METERS
        val targetLat = spawnLat + Math.toDegrees(dLatRad)

        // Delta Lng from X: X = dLng_rad * R * cos(avgLat)
        val avgLatRad = Math.toRadians((targetLat + spawnLat) / 2.0)
        val cosFactor = cos(avgLatRad).coerceAtLeast(0.0001)
        val dLngRad = mcCoords.x.toDouble() / (EARTH_RADIUS_METERS * cosFactor)
        val targetLng = spawnLng + Math.toDegrees(dLngRad)

        val targetAlt = spawnAlt + (mcCoords.y - DEFAULT_SPAWN_Y)

        return GpsLocation(
            latitude = targetLat,
            longitude = targetLng,
            altitude = targetAlt
        )
    }

    /**
     * Calculates 2D Euclidean distance in Minecraft blocks (meters) from spawn or between two points.
     */
    fun distanceInBlocks(x1: Int, z1: Int, x2: Int = 0, z2: Int = 0): Double {
        val dx = (x1 - x2).toDouble()
        val dz = (z1 - z2).toDouble()
        return sqrt(dx * dx + dz * dz)
    }

    /**
     * Calculates 3D distance in Minecraft blocks.
     */
    fun distance3DInBlocks(c1: MinecraftCoords, c2: MinecraftCoords): Double {
        val dx = (c1.x - c2.x).toDouble()
        val dy = (c1.y - c2.y).toDouble()
        val dz = (c1.z - c2.z).toDouble()
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    /**
     * Gets compass cardinal direction string from point A to point B.
     */
    fun compassDirection(targetX: Int, targetZ: Int, originX: Int = 0, originZ: Int = 0): String {
        val dx = targetX - originX // East (+) / West (-)
        val dz = targetZ - originZ // South (+) / North (-)

        // In standard map: North is up (-Z), East is right (+X)
        // angle in degrees where 0 = North, 90 = East, 180 = South, 270 = West
        val angleRad = atan2(dx.toDouble(), -dz.toDouble())
        var degrees = Math.toDegrees(angleRad)
        if (degrees < 0) degrees += 360.0

        return when {
            degrees >= 337.5 || degrees < 22.5 -> "N (Norte)"
            degrees >= 22.5 && degrees < 67.5 -> "NE (Noreste)"
            degrees >= 67.5 && degrees < 112.5 -> "E (Este)"
            degrees >= 112.5 && degrees < 157.5 -> "SE (Sureste)"
            degrees >= 157.5 && degrees < 202.5 -> "S (Sur)"
            degrees >= 202.5 && degrees < 247.5 -> "SO (Suroeste)"
            degrees >= 247.5 && degrees < 292.5 -> "O (Oeste)"
            else -> "NO (Noroeste)"
        }
    }

    /**
     * Estimates Minecraft biome based on elevation and coordinate variations
     */
    fun estimateBiome(coords: MinecraftCoords): String {
        return when {
            coords.y > 140 -> "Cumbres Nevadas (Jagged Peaks)"
            coords.y > 100 -> "Montañas Pedregosas (Stony Peaks)"
            coords.y < 50 -> "Cavernas Profundas (Deep Caves)"
            (coords.x + coords.z) % 7 == 0 -> "Bosque de Robles (Oak Forest)"
            (coords.x + coords.z) % 5 == 0 -> "Pradera Floreciente (Meadow)"
            (coords.x - coords.z) % 9 == 0 -> "Taiga Antigua (Old Growth Taiga)"
            else -> "Llanuras Voxel (Plains)"
        }
    }
}
