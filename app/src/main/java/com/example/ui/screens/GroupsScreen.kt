package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.converter.CoordinateConverter
import com.example.data.model.GroupMember
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
import com.example.ui.theme.NetherPortalPurple
import com.example.ui.theme.RedstoneAccent
import com.example.ui.theme.StoneOutline

@Composable
fun GroupsScreen(
    activeRealm: GroupRealm?,
    allRealms: List<GroupRealm>,
    members: List<GroupMember>,
    userProfile: UserProfile,
    currentUserCoords: MinecraftCoords,
    userGpsLat: Double,
    userGpsLng: Double,
    userGpsAlt: Double,
    onCreateRealm: (name: String, desc: String, useCurrentGps: Boolean, lat: Double, lng: Double, alt: Double) -> Unit,
    onJoinRealm: (code: String) -> Unit,
    onSwitchRealm: (realmId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var showSwitchDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF13181E))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Realm Header Card
        item {
            activeRealm?.let { realm ->
                MinecraftBlockCard(
                    borderColor = GoldAccent,
                    containerColor = Color(0xFF19202B),
                    modifier = Modifier.testTag("active_realm_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "REINO ACTIVO",
                                    color = GoldAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = realm.name,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Join Code Badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF283442),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DiamondCyan),
                            modifier = Modifier.clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Código de Reino", realm.code))
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = realm.code,
                                    color = DiamondCyan,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copiar Código",
                                    tint = DiamondCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    if (realm.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = realm.description,
                            color = Color(0xFFA0AEC0),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // World Spawn (0, 64, 0) Info Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF384758), RoundedCornerShape(4.dp))
                            .background(Color(0xFF141A22), RoundedCornerShape(4.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Stars,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "WORLD SPAWN EN CERO",
                                        color = GoldAccent,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                MinecraftBadge(text = "X:0 | Y:64 | Z:0", color = GrassGreenPrimary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ubicación real de referencia:",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Lat: ${String.format("%.6f", realm.spawnLat)}, Lng: ${String.format("%.6f", realm.spawnLng)} (Alt: ${realm.spawnAlt.toInt()}m)",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            } ?: run {
                Text(
                    text = "Cargando Reino...",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Action Buttons Row (Crear Reino / Unirse / Cambiar)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MinecraftButton(
                    text = "Nuevo Reino",
                    onClick = { showCreateDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f).testTag("btn_create_realm")
                )
                MinecraftButton(
                    text = "Unirse",
                    onClick = { showJoinDialog = true },
                    color = DiamondCyan,
                    textColor = Color(0xFF12161C),
                    icon = { Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f).testTag("btn_join_realm")
                )
                if (allRealms.size > 1) {
                    MinecraftButton(
                        text = "Cambiar",
                        onClick = { showSwitchDialog = true },
                        color = Color(0xFF2C3848),
                        modifier = Modifier.weight(0.9f)
                    )
                }
            }
        }

        // Section Title: Companions / Members
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        tint = DiamondCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "COMPAÑEROS EN EL MUNDO (${members.size + 1})",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "Sincronizado en Vivo",
                    color = GrassGreenPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Current User Member Card
        item {
            MemberItemCard(
                gamertag = "${userProfile.gamertag} (Tú)",
                skinPreset = userProfile.skinPreset,
                customSkinUri = userProfile.customSkinUri,
                mcCoords = currentUserCoords,
                status = "En línea",
                isCurrentUser = true,
                distFromUser = 0
            )
        }

        // Other Members Cards
        items(members) { member ->
            val dist = CoordinateConverter.distanceInBlocks(
                member.mcX, member.mcZ,
                currentUserCoords.x, currentUserCoords.z
            ).toInt()

            MemberItemCard(
                gamertag = member.gamertag,
                skinPreset = member.skinPreset,
                customSkinUri = member.customSkinUri,
                mcCoords = MinecraftCoords(member.mcX, member.mcY, member.mcZ),
                status = member.status,
                isCurrentUser = false,
                distFromUser = dist
            )
        }
    }

    // Dialog: Create Realm
    if (showCreateDialog) {
        CreateRealmDialog(
            userGpsLat = userGpsLat,
            userGpsLng = userGpsLng,
            userGpsAlt = userGpsAlt,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, desc, useCurrent, lat, lng, alt ->
                onCreateRealm(name, desc, useCurrent, lat, lng, alt)
                showCreateDialog = false
            }
        )
    }

    // Dialog: Join Realm by Code
    if (showJoinDialog) {
        JoinRealmDialog(
            onDismiss = { showJoinDialog = false },
            onConfirm = { code ->
                onJoinRealm(code)
                showJoinDialog = false
            }
        )
    }

    // Dialog: Switch Realm
    if (showSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchDialog = false },
            containerColor = DeepslateCard,
            title = { Text("Cambiar de Reino", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allRealms.forEach { realm ->
                        val isCurrent = realm.id == activeRealm?.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = if (isCurrent) GoldAccent else Color(0xFF384353),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .background(
                                    color = if (isCurrent) GoldAccent.copy(alpha = 0.2f) else Color(0xFF1E252E),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    onSwitchRealm(realm.id)
                                    showSwitchDialog = false
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(realm.name, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("Código: ${realm.code}", color = DiamondCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                                if (isCurrent) {
                                    MinecraftBadge(text = "ACTIVO", color = GoldAccent)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSwitchDialog = false }) {
                    Text("Cerrar", color = Color(0xFFA0AEC0))
                }
            }
        )
    }
}

@Composable
fun MemberItemCard(
    gamertag: String,
    skinPreset: String,
    customSkinUri: String?,
    mcCoords: MinecraftCoords,
    status: String,
    isCurrentUser: Boolean,
    distFromUser: Int
) {
    val distToSpawn = CoordinateConverter.distanceInBlocks(mcCoords.x, mcCoords.z).toInt()
    val borderColor = if (isCurrentUser) GrassGreenPrimary else StoneOutline

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(6.dp))
            .testTag("member_card_${gamertag}"),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) Color(0xFF192520) else Color(0xFF1B222B)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Minecraft Skin Avatar Head
            SkinAvatar(
                skinPreset = skinPreset,
                customSkinUri = customSkinUri,
                size = 52.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = gamertag,
                        color = if (isCurrentUser) GrassGreenPrimary else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    MinecraftBadge(
                        text = status,
                        color = if (isCurrentUser) GrassGreenPrimary else DiamondCyan
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Block Coordinates
                Text(
                    text = "X: ${mcCoords.x} | Y: ${mcCoords.y} | Z: ${mcCoords.z}",
                    color = Color(0xFFE2E8F0),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Spawn: ${distToSpawn}m",
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    if (!isCurrentUser) {
                        Text(
                            text = "A ${distFromUser}m de ti",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateRealmDialog(
    userGpsLat: Double,
    userGpsLng: Double,
    userGpsAlt: Double,
    onDismiss: () -> Unit,
    onConfirm: (name: String, desc: String, useCurrent: Boolean, lat: Double, lng: Double, alt: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var useCurrentGps by remember { mutableStateOf(true) }
    var customLatStr by remember { mutableStateOf(userGpsLat.toString()) }
    var customLngStr by remember { mutableStateOf(userGpsLng.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepslateCard,
        title = {
            Text("Crear Nuevo Reino con Amigos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Reino (Ej: Servidor Amigos)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GrassGreenPrimary,
                        unfocusedBorderColor = Color(0xFF404D5E)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_realm_name")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Descripción") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GrassGreenPrimary,
                        unfocusedBorderColor = Color(0xFF404D5E)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Configuración del World Spawn (Punto 0, 64, 0):",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { useCurrentGps = true }
                ) {
                    RadioButton(
                        selected = useCurrentGps,
                        onClick = { useCurrentGps = true },
                        colors = RadioButtonDefaults.colors(selectedColor = GrassGreenPrimary)
                    )
                    Text("Usar mi ubicación GPS actual como Spawn", color = Color.White, fontSize = 13.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { useCurrentGps = false }
                ) {
                    RadioButton(
                        selected = !useCurrentGps,
                        onClick = { useCurrentGps = false },
                        colors = RadioButtonDefaults.colors(selectedColor = GrassGreenPrimary)
                    )
                    Text("Ingresar coordenadas GPS personalizadas", color = Color.White, fontSize = 13.sp)
                }

                if (!useCurrentGps) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customLatStr,
                        onValueChange = { customLatStr = it },
                        label = { Text("Latitud Spawn") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customLngStr,
                        onValueChange = { customLngStr = it },
                        label = { Text("Longitud Spawn") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            MinecraftButton(
                text = "Crear Reino",
                onClick = {
                    val lat = customLatStr.toDoubleOrNull() ?: userGpsLat
                    val lng = customLngStr.toDoubleOrNull() ?: userGpsLng
                    onConfirm(name.ifBlank { "Reino Nuevo" }, desc, useCurrentGps, lat, lng, userGpsAlt)
                },
                modifier = Modifier.testTag("btn_confirm_create_realm")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color(0xFFA0AEC0)) }
        }
    )
}

@Composable
fun JoinRealmDialog(
    onDismiss: () -> Unit,
    onConfirm: (code: String) -> Unit
) {
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepslateCard,
        title = {
            Text("Unirse a un Reino", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column {
                Text(
                    text = "Ingresa el código compartido por tu amigo (ej: MC-777):",
                    color = Color(0xFFD1D5DB),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("Código de Reino") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = DiamondCyan,
                        unfocusedBorderColor = Color(0xFF404D5E)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_realm_code")
                )
            }
        },
        confirmButton = {
            MinecraftButton(
                text = "Unirse",
                onClick = { onConfirm(code) },
                color = DiamondCyan,
                textColor = Color(0xFF13181E),
                modifier = Modifier.testTag("btn_confirm_join_realm")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color(0xFFA0AEC0)) }
        }
    )
}
