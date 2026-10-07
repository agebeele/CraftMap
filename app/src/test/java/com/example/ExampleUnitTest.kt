package com.example

import com.example.data.converter.CoordinateConverter
import com.example.data.model.MinecraftCoords
import com.example.ui.map.MapTileLayer
import com.example.ui.map.TileMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ExampleUnitTest {
    private val spawnLat = 19.432608
    private val spawnLng = -99.133209
    private val spawnAlt = 2240.0

    @Test
    fun testSpawnOriginIsZeros() {
        val coords = CoordinateConverter.gpsToMinecraft(
            targetLat = spawnLat,
            targetLng = spawnLng,
            targetAlt = spawnAlt,
            spawnLat = spawnLat,
            spawnLng = spawnLng,
            spawnAlt = spawnAlt
        )
        assertEquals(0, coords.x)
        assertEquals(64, coords.y)
        assertEquals(0, coords.z)
    }

    @Test
    fun testEastAndNorthDirections() {
        val eastCoords = CoordinateConverter.gpsToMinecraft(
            targetLat = spawnLat,
            targetLng = spawnLng + 0.001,
            targetAlt = spawnAlt,
            spawnLat = spawnLat,
            spawnLng = spawnLng,
            spawnAlt = spawnAlt
        )
        assertTrue("East should be positive X", eastCoords.x > 0)

        val northCoords = CoordinateConverter.gpsToMinecraft(
            targetLat = spawnLat + 0.001,
            targetLng = spawnLng,
            targetAlt = spawnAlt,
            spawnLat = spawnLat,
            spawnLng = spawnLng,
            spawnAlt = spawnAlt
        )
        assertTrue("North should be negative Z in Minecraft", northCoords.z < 0)
    }

    @Test
    fun testNetherCoordinateRatio() {
        val overworldCoords = MinecraftCoords(x = 800, y = 64, z = -1600)
        assertEquals(100, overworldCoords.netherX)
        assertEquals(-200, overworldCoords.netherZ)
    }

    @Test
    fun testRoundtripConversion() {
        val initialCoords = MinecraftCoords(x = 250, y = 70, z = -300)
        val gps = CoordinateConverter.minecraftToGps(
            mcCoords = initialCoords,
            spawnLat = spawnLat,
            spawnLng = spawnLng,
            spawnAlt = spawnAlt
        )
        val roundtrip = CoordinateConverter.gpsToMinecraft(
            targetLat = gps.latitude,
            targetLng = gps.longitude,
            targetAlt = gps.altitude,
            spawnLat = spawnLat,
            spawnLng = spawnLng,
            spawnAlt = spawnAlt
        )

        assertTrue("X should match within 1 block", abs(roundtrip.x - initialCoords.x) <= 1)
        assertEquals(initialCoords.y, roundtrip.y)
        assertTrue("Z should match within 1 block", abs(roundtrip.z - initialCoords.z) <= 1)
    }

    @Test
    fun testTileMathRoundtrip() {
        val zoom = 16
        val tileX = TileMath.lngToTileX(spawnLng, zoom)
        val tileY = TileMath.latToTileY(spawnLat, zoom)

        val recoveredLng = TileMath.tileXToLng(tileX, zoom)
        val recoveredLat = TileMath.tileYToLat(tileY, zoom)

        assertTrue("Lng roundtrip matches", abs(spawnLng - recoveredLng) < 0.00001)
        assertTrue("Lat roundtrip matches", abs(spawnLat - recoveredLat) < 0.00001)
    }

    @Test
    fun testTileUrlGeneration() {
        val satUrl = MapTileLayer.SATELLITE.getTileUrl(100, 200, 10)
        assertTrue("Satellite url contains arcgisonline", satUrl.contains("arcgisonline.com"))

        val streetUrl = MapTileLayer.STREETS.getTileUrl(100, 200, 10)
        assertTrue("Street url contains openstreetmap", streetUrl.contains("openstreetmap.org"))
    }
}
