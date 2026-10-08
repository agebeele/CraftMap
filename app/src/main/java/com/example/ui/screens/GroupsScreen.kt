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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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

import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import com.example.data.model.RealmInvite

@Composable
fun GroupsScreen(
    activeRealm: GroupRealm?,
    allRealms: List<GroupRealm>,
    members: List<GroupMember>,
    invites: List<RealmInvite> = emptyList(),
    userProfile: UserProfile,
    currentUserCoords: MinecraftCoords,
    userGpsLat: Double,
    userGpsLng: Double,
    userGpsAlt: Double,
    onCreateRealm: (name: String, desc: String, useCurrentGps: Boolean, lat: Double, lng: Double, alt: Double, password: String) -> Unit,
    onJoinRealm: (code: String, password: String) -> Unit,
    onSwitchRealm: (realmId: String) -> Unit,
    onSendInvite: (gamertag: String, realm: GroupRealm) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var showSwitchDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var promptInviteForPassword by remember { mutableStateOf<RealmInvite?>(null) }

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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

                // Dedicated INVITE FRIEND button with notification integration
                activeRealm?.let { realm ->
                    MinecraftButton(
                        text = "✉️ Invitar Amigo por Gamertag (Notificación)",
                        onClick = { showInviteDialog = true },
                        color = GoldAccent,
                        textColor = Color(0xFF141922),
                        icon = { Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.fillMaxWidth().testTag("btn_invite_friend")
                    )
                }
            }
        }

        // Invites List Section
        if (invites.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "INVITACIONES DE AMIGOS (${invites.size})",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            items(invites) { inv ->
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B232D)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(inv.realmName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                MinecraftBadge(text = inv.status, color = if (inv.status == "Enviada") GoldAccent else DiamondCyan)
                                if (inv.requiresPassword) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    MinecraftBadge(text = "🔒 Protegido", color = GoldAccent)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "De @${inv.senderGamertag} para @${inv.targetGamertag}",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Código: ${inv.realmCode}",
                                color = DiamondCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        MinecraftButton(
                            text = "Conectar",
                            onClick = {
                                if (inv.requiresPassword) {
                                    promptInviteForPassword = inv
                                } else {
                                    onJoinRealm(inv.realmCode, "")
                                }
                            },
                            color = GrassGreenPrimary,
                            modifier = Modifier.height(34.dp)
                        )
                    }
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
            onConfirm = { name, desc, useCurrent, lat, lng, alt, password ->
                onCreateRealm(name, desc, useCurrent, lat, lng, alt, password)
                showCreateDialog = false
            }
        )
    }

    // Dialog: Join Realm by Code
    if (showJoinDialog) {
        JoinRealmDialog(
            onDismiss = { showJoinDialog = false },
            onConfirm = { code, password ->
                onJoinRealm(code, password)
                showJoinDialog = false
            }
        )
    }

    // Dialog: Prompt Password for Protected Invite
    promptInviteForPassword?.let { invite ->
        var invitePass by remember { mutableStateOf(invite.realmPassword) }
        var showInvitePass by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { promptInviteForPassword = null },
            containerColor = DeepslateCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Contraseña del Reino", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "El reino '${invite.realmName}' requiere contraseña para unirse. Ingresa la clave de acceso:",
                        color = Color(0xFFD1D5DB),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = invitePass,
                        onValueChange = { invitePass = it },
                        label = { Text("Contraseña de Acceso") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { showInvitePass = !showInvitePass }) {
                                Icon(
                                    if (showInvitePass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color(0xFFA0AEC0)
                                )
                            }
                        },
                        visualTransformation = if (showInvitePass) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = Color(0xFF404D5E)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_invite_password")
                    )
                }
            },
            confirmButton = {
                MinecraftButton(
                    text = "Unirse al Reino",
                    onClick = {
                        onJoinRealm(invite.realmCode, invitePass.trim())
                        promptInviteForPassword = null
                    },
                    color = GrassGreenPrimary
                )
            },
            dismissButton = {
                TextButton(onClick = { promptInviteForPassword = null }) {
                    Text("Cancelar", color = Color(0xFFA0AEC0))
                }
            }
        )
    }

    // Dialog: Invite Friend by Gamertag
    if (showInviteDialog && activeRealm != null) {
        InviteFriendDialog(
            realm = activeRealm,
            onDismiss = { showInviteDialog = false },
            onConfirm = { gamertag ->
                onSendInvite(gamertag, activeRealm)
                showInviteDialog = false
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
fun InviteFriendDialog(
    realm: GroupRealm,
    onDismiss: () -> Unit,
    onConfirm: (gamertag: String) -> Unit
) {
    var gamertag by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepslateCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✉️ Invitar a ${realm.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column {
                Text(
                    text = "Código de Reino: ${realm.code}",
                    color = DiamondCyan,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ingresa el Gamertag de Minecraft de tu compañero. La app enviará la invitación e instantáneamente generará una notificación del sistema Android en su teléfono con el enlace al World Spawn:",
                    color = Color(0xFFD1D5DB),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = gamertag,
                    onValueChange = { gamertag = it },
                    label = { Text("Gamertag del amigo (ej: Steve, MineKing99)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = Color(0xFF404D5E)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_invite_gamertag")
                )

                if (realm.password.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0x2EF6AD55),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Reino protegido con contraseña. Tu compañero recibirá la clave de acceso en la invitación.",
                                color = GoldAccent,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            MinecraftButton(
                text = "Enviar Invitación y Notificar",
                onClick = {
                    if (gamertag.isNotBlank()) {
                        onConfirm(gamertag.trim())
                    }
                },
                color = GoldAccent,
                textColor = Color(0xFF141A22),
                modifier = Modifier.testTag("btn_confirm_invite")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color(0xFFA0AEC0))
            }
        }
    )
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
    onConfirm: (name: String, desc: String, useCurrent: Boolean, lat: Double, lng: Double, alt: Double, password: String) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var useCurrentGps by remember { mutableStateOf(false) } // Default to letting user set exact coords!
    var mapsCombinedStr by remember { mutableStateOf("") }
    var customLatStr by remember { mutableStateOf(String.format(java.util.Locale.US, "%.6f", userGpsLat)) }
    var customLngStr by remember { mutableStateOf(String.format(java.util.Locale.US, "%.6f", userGpsLng)) }

    fun parseAndApplyCoords(input: String) {
        val parts = input.trim().split(Regex("[,;\\s]+")).filter { it.isNotBlank() }
        if (parts.size >= 2) {
            val pLat = parts[0].toDoubleOrNull()
            val pLng = parts[1].toDoubleOrNull()
            if (pLat != null && pLng != null) {
                customLatStr = pLat.toString()
                customLngStr = pLng.toString()
            }
        }
    }

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
                    Text("Usar mi ubicación GPS actual (${String.format("%.4f", userGpsLat)}, ${String.format("%.4f", userGpsLng)})", color = Color.White, fontSize = 12.sp)
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
                    Text("Pegar coordenadas de Google Maps (Recomendado)", color = Color.White, fontSize = 12.sp)
                }

                if (!useCurrentGps) {
                    Spacer(modifier = Modifier.height(6.dp))

                    // Easy Google Maps paste input: handles "lat, lng" directly
                    OutlinedTextField(
                        value = mapsCombinedStr,
                        onValueChange = {
                            mapsCombinedStr = it
                            parseAndApplyCoords(it)
                        },
                        label = { Text("Pegar de Google Maps (ej: 19.432608, -99.133209)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = Color(0xFF404D5E)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_maps_combined")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customLatStr,
                            onValueChange = { customLatStr = it },
                            label = { Text("Latitud Spawn") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f).testTag("input_spawn_lat")
                        )
                        OutlinedTextField(
                            value = customLngStr,
                            onValueChange = { customLngStr = it },
                            label = { Text("Longitud Spawn") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f).testTag("input_spawn_lng")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Seguridad del Reino (Contraseña Opcional):",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña para ingresar (Opcional)") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "Ocultar" else "Mostrar",
                                tint = Color(0xFFA0AEC0)
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = Color(0xFF404D5E)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_realm_password")
                )

                Text(
                    text = "🔒 Si estableces una contraseña, los amigos deberán ingresarla al unirse.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            MinecraftButton(
                text = "Crear Reino y Fijar Spawn",
                onClick = {
                    val lat = customLatStr.toDoubleOrNull() ?: userGpsLat
                    val lng = customLngStr.toDoubleOrNull() ?: userGpsLng
                    onConfirm(name.ifBlank { "Reino Minecraft" }, desc, useCurrentGps, lat, lng, userGpsAlt, password.trim())
                },
                color = GrassGreenPrimary,
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
    onConfirm: (code: String, password: String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

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

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña (si el reino tiene)") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "Ocultar" else "Mostrar",
                                tint = Color(0xFFA0AEC0)
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = DiamondCyan,
                        unfocusedBorderColor = Color(0xFF404D5E)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_join_password")
                )
            }
        },
        confirmButton = {
            MinecraftButton(
                text = "Unirse",
                onClick = { onConfirm(code, password.trim()) },
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
