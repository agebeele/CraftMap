package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GroupRealm
import com.example.data.model.MinecraftCoords
import com.example.data.model.UserProfile
import com.example.ui.components.MinecraftBadge
import com.example.ui.components.MinecraftBlockCard
import com.example.ui.components.MinecraftButton
import com.example.ui.components.SkinAvatar
import com.example.ui.theme.DeepslateCard
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GrassGreenPrimary
import com.example.ui.theme.RedstoneAccent
import com.example.ui.theme.StoneOutline

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    currentUserCoords: MinecraftCoords,
    activeRealm: GroupRealm?,
    onSaveProfile: (gamertag: String, skinPreset: String, customSkinUri: String?, mcUsername: String?) -> Unit,
    onRemoveCustomSkin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var gamertag by remember(userProfile) { mutableStateOf(userProfile.gamertag) }
    var selectedPreset by remember(userProfile) { mutableStateOf(userProfile.skinPreset) }
    var customSkinUri by remember(userProfile) { mutableStateOf(userProfile.customSkinUri) }
    var minecraftUsername by remember(userProfile) { mutableStateOf(userProfile.minecraftUsername ?: "") }

    // Android Photo Picker (zero-permission, compliant with Google Play policies)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            customSkinUri = uri.toString()
        }
    }

    val availablePresets = listOf(
        "steve" to "Steve Clásico",
        "alex" to "Alex Aventurera",
        "diamond_knight" to "Caballero Diamante",
        "creeper" to "Creeper Guy",
        "enderman" to "Enderman",
        "miner" to "Minero Antorcha"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF13181E))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "PERFIL DE JUGADOR",
                    color = GoldAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Identidad y Skin de Minecraft",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Configura tu Gamertag y tu avatar para que tus compañeros te reconozcan en el mapa.",
                    color = Color(0xFFA0AEC0),
                    fontSize = 12.sp
                )
            }
        }

        // Live Preview Hero Card
        item {
            MinecraftBlockCard(
                borderColor = GoldAccent,
                containerColor = Color(0xFF1A232F)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Big Head Preview
                    SkinAvatar(
                        skinPreset = selectedPreset,
                        customSkinUri = customSkinUri,
                        minecraftUsername = minecraftUsername.takeIf { it.isNotBlank() },
                        size = 80.dp,
                        modifier = Modifier.testTag("profile_avatar_preview")
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = gamertag.ifBlank { "Jugador" },
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Posición actual: [X: ${currentUserCoords.x}, Y: ${currentUserCoords.y}, Z: ${currentUserCoords.z}]",
                            color = DiamondCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Reino: ${activeRealm?.name ?: "Sin Reino"}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        if (!customSkinUri.isNullOrBlank()) {
                            MinecraftBadge(text = "Skin Personalizada Activa", color = GrassGreenPrimary)
                        } else {
                            MinecraftBadge(text = "Preset: $selectedPreset", color = GoldAccent)
                        }
                    }
                }
            }
        }

        // Gamertag Form
        item {
            MinecraftBlockCard(borderColor = StoneOutline) {
                Text(
                    text = "1. Gamertag / Nombre en el Servidor",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = gamertag,
                    onValueChange = { gamertag = it },
                    label = { Text("Tu Gamertag (libre elección)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GrassGreenPrimary,
                        unfocusedBorderColor = Color(0xFF404D5E)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_profile_gamertag")
                )
            }
        }

        // Skin Image Upload Section
        item {
            MinecraftBlockCard(borderColor = DiamondCyan) {
                Text(
                    text = "2. Subir Skin Personalizada desde la Galería",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Sube una imagen o archivo .png de tu skin de Minecraft. Se recortará automáticamente para el mapa.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MinecraftButton(
                        text = "Subir Imagen",
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        icon = { Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        color = DiamondCyan,
                        textColor = Color(0xFF13181E),
                        modifier = Modifier.weight(1f).testTag("btn_upload_skin")
                    )

                    if (!customSkinUri.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = {
                                customSkinUri = null
                                onRemoveCustomSkin()
                            },
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RedstoneAccent),
                            modifier = Modifier.testTag("btn_remove_skin")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = RedstoneAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Quitar", color = RedstoneAccent, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Fetch Skin by Official Minecraft Java/Bedrock Nickname
        item {
            MinecraftBlockCard(borderColor = StoneOutline) {
                Text(
                    text = "3. O Vincular por Gamertag Oficial de Minecraft",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ingresa tu usuario de Java/Bedrock para descargar automáticamente tu cabeza oficial (Minotar API):",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = minecraftUsername,
                    onValueChange = { minecraftUsername = it },
                    label = { Text("Usuario de Minecraft (ej: Steve, Alex, Notch)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = DiamondCyan,
                        unfocusedBorderColor = Color(0xFF404D5E)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_mc_username")
                )
            }
        }

        // Built-in Skin Presets
        item {
            MinecraftBlockCard(borderColor = StoneOutline) {
                Text(
                    text = "4. O Elige un Avatar Integrado",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availablePresets) { (presetKey, label) ->
                        val isSelected = selectedPreset == presetKey && customSkinUri.isNullOrBlank() && minecraftUsername.isBlank()
                        Box(
                            modifier = Modifier
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) GoldAccent else Color(0xFF384353),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .background(
                                    color = if (isSelected) GoldAccent.copy(alpha = 0.2f) else Color(0xFF1E252E),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    selectedPreset = presetKey
                                    customSkinUri = null
                                    minecraftUsername = ""
                                }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                SkinAvatar(
                                    skinPreset = presetKey,
                                    size = 48.dp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else Color(0xFFA0AEC0),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Save & Sync Button
        item {
            MinecraftButton(
                text = "Guardar y Sincronizar Perfil",
                onClick = {
                    onSaveProfile(
                        gamertag,
                        selectedPreset,
                        customSkinUri,
                        minecraftUsername.takeIf { it.isNotBlank() }
                    )
                },
                color = GrassGreenPrimary,
                icon = { Icon(Icons.Default.Check, contentDescription = null, tint = Color.White) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_profile")
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
