package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun SkinAvatar(
    skinPreset: String,
    customSkinUri: String? = null,
    minecraftUsername: String? = null,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val borderColor = Color(0xFF1E232A)

    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, RoundedCornerShape(4.dp))
            .border(2.dp, borderColor, RoundedCornerShape(4.dp))
            .background(Color(0xFF282E37), RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .testTag("skin_avatar_${skinPreset}"),
        contentAlignment = Alignment.Center
    ) {
        if (!customSkinUri.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Uri.parse(customSkinUri))
                    .crossfade(true)
                    .build(),
                contentDescription = "Minecraft Skin",
                filterQuality = FilterQuality.None,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )
        } else if (!minecraftUsername.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://mc-heads.net/avatar/$minecraftUsername/128")
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar de $minecraftUsername",
                filterQuality = FilterQuality.None,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )
        } else {
            // Built-in Pixel-Art Minecraft Face Canvas
            MinecraftPixelHead(
                preset = skinPreset,
                modifier = Modifier.size(size)
            )
        }
    }
}

@Composable
fun MinecraftPixelHead(
    preset: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cellW = w / 8f
        val cellH = h / 8f

        fun drawCell(x: Int, y: Int, color: Color) {
            drawRect(
                color = color,
                topLeft = Offset(x * cellW, y * cellH),
                size = Size(cellW + 0.5f, cellH + 0.5f)
            )
        }

        when (preset.lowercase()) {
            "alex" -> {
                // Alex ginger hair & green eyes
                val hair = Color(0xFFD67828)
                val skin = Color(0xFFF0BD96)
                val eyes = Color(0xFF45A162)
                val white = Color.White
                val lips = Color(0xFFC76D6D)

                // Row 0-2 Hair
                for (r in 0..2) for (c in 0..7) drawCell(c, r, hair)
                // Row 3: Hair side, skin forehead
                drawCell(0, 3, hair); drawCell(7, 3, hair)
                for (c in 1..6) drawCell(c, 3, skin)
                // Row 4: Eyes
                drawCell(0, 4, hair); drawCell(7, 4, hair)
                drawCell(1, 4, white); drawCell(2, 4, eyes)
                drawCell(3, 4, skin); drawCell(4, 4, skin)
                drawCell(5, 4, eyes); drawCell(6, 4, white)
                // Row 5: Cheeks & nose
                drawCell(0, 5, hair); drawCell(7, 5, hair)
                for (c in 1..6) drawCell(c, 5, skin)
                // Row 6: Mouth
                drawCell(0, 6, hair); drawCell(7, 6, hair)
                drawCell(1, 6, skin); drawCell(2, 6, skin)
                drawCell(3, 6, lips); drawCell(4, 6, lips)
                drawCell(5, 6, skin); drawCell(6, 6, skin)
                // Row 7: Chin
                drawCell(0, 7, hair); drawCell(7, 7, hair)
                for (c in 1..6) drawCell(c, 7, skin)
            }
            "diamond_knight" -> {
                // Diamond Helmet & glowing cyan eyes
                val diamond = Color(0xFF4AEDD7)
                val diamondDark = Color(0xFF238E80)
                val visor = Color(0xFF162529)
                val cyanGlow = Color(0xFF70FFF0)

                for (r in 0..7) for (c in 0..7) drawCell(c, r, diamond)
                // Helmet shading
                drawCell(0, 0, diamondDark); drawCell(7, 0, diamondDark)
                drawCell(0, 7, diamondDark); drawCell(7, 7, diamondDark)
                // Visor slit
                for (c in 2..5) {
                    drawCell(c, 3, visor)
                    drawCell(c, 4, visor)
                }
                drawCell(2, 4, cyanGlow)
                drawCell(5, 4, cyanGlow)
            }
            "creeper" -> {
                // Creeper iconic face
                val greenLight = Color(0xFF55A33D)
                val greenDark = Color(0xFF3B7A27)
                val black = Color(0xFF1E2819)

                for (r in 0..7) for (c in 0..7) {
                    val baseCol = if ((r + c) % 2 == 0) greenLight else greenDark
                    drawCell(c, r, baseCol)
                }
                // Eyes
                drawCell(1, 2, black); drawCell(2, 2, black)
                drawCell(5, 2, black); drawCell(6, 2, black)
                drawCell(1, 3, black); drawCell(2, 3, black)
                drawCell(5, 3, black); drawCell(6, 3, black)
                // Mouth
                drawCell(3, 4, black); drawCell(4, 4, black)
                drawCell(2, 5, black); drawCell(3, 5, black); drawCell(4, 5, black); drawCell(5, 5, black)
                drawCell(2, 6, black); drawCell(5, 6, black)
                drawCell(2, 7, black); drawCell(5, 7, black)
            }
            "enderman" -> {
                // Enderman obsidian head + purple eyes
                val obsidian = Color(0xFF121216)
                val obsidian2 = Color(0xFF1B1B22)
                val purple = Color(0xFFB549E0)
                val lightPurple = Color(0xFFE28CFF)

                for (r in 0..7) for (c in 0..7) {
                    drawCell(c, r, if ((r * 3 + c) % 2 == 0) obsidian else obsidian2)
                }
                // Purple eyes
                drawCell(1, 4, lightPurple); drawCell(2, 4, purple)
                drawCell(5, 4, purple); drawCell(6, 4, lightPurple)
            }
            "miner" -> {
                // Miner helmet with lamp
                val hat = Color(0xFF3D3F43)
                val lamp = Color(0xFFFFD700)
                val skin = Color(0xFFDCB088)
                val eyes = Color(0xFF3B5998)
                val beard = Color(0xFF523318)

                for (r in 0..2) for (c in 0..7) drawCell(c, r, hat)
                // Gold lamp
                drawCell(3, 1, lamp); drawCell(4, 1, lamp)
                drawCell(3, 2, lamp); drawCell(4, 2, lamp)
                // Face
                for (r in 3..7) for (c in 0..7) drawCell(c, r, skin)
                // Eyes
                drawCell(1, 4, Color.White); drawCell(2, 4, eyes)
                drawCell(5, 4, eyes); drawCell(6, 4, Color.White)
                // Beard
                for (c in 2..5) drawCell(c, 6, beard)
                for (c in 1..6) drawCell(c, 7, beard)
            }
            else -> {
                // Classic Steve
                val hair = Color(0xFF4A3222)
                val skin = Color(0xFFBE8B68)
                val eyes = Color(0xFF2C4482)
                val white = Color.White
                val beard = Color(0xFF6B442B)

                // Hair
                for (r in 0..2) for (c in 0..7) drawCell(c, r, hair)
                drawCell(0, 3, hair); drawCell(7, 3, hair)
                for (c in 1..6) drawCell(c, 3, skin)
                // Eyes
                drawCell(0, 4, hair); drawCell(7, 4, hair)
                drawCell(1, 4, white); drawCell(2, 4, eyes)
                drawCell(3, 4, skin); drawCell(4, 4, skin)
                drawCell(5, 4, eyes); drawCell(6, 4, white)
                // Nose/Cheeks
                drawCell(0, 5, skin); drawCell(7, 5, skin)
                for (c in 1..6) drawCell(c, 5, skin)
                // Mouth/Goatee
                drawCell(0, 6, skin); drawCell(7, 6, skin)
                drawCell(1, 6, skin); drawCell(2, 6, beard)
                drawCell(3, 6, beard); drawCell(4, 6, beard)
                drawCell(5, 6, beard); drawCell(6, 6, skin)
                // Chin
                drawCell(0, 7, skin); drawCell(7, 7, skin)
                for (c in 1..6) drawCell(c, 7, beard)
            }
        }
    }
}
