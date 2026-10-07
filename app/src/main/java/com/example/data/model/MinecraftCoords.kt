package com.example.data.model

data class MinecraftCoords(
    val x: Int,
    val y: Int,
    val z: Int
) {
    val netherX: Int get() = kotlin.math.floor(x / 8.0).toInt()
    val netherZ: Int get() = kotlin.math.floor(z / 8.0).toInt()
    val chunkX: Int get() = x shr 4
    val chunkZ: Int get() = z shr 4
    val chunkOffsetLocalX: Int get() = (x and 15)
    val chunkOffsetLocalZ: Int get() = (z and 15)

    fun formatted(): String = "X: $x | Y: $y | Z: $z"
    fun netherFormatted(): String = "Nether: [X: $netherX, Y: $y, Z: $netherZ]"
}

data class GpsLocation(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0
)
