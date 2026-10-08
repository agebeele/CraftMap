package com.example.data.model

data class GroupRealm(
    val id: String,
    val name: String,
    val code: String, // 6-digit code for friends to join
    val description: String = "",
    val password: String = "", // Optional access password for joining
    val spawnLat: Double,
    val spawnLng: Double,
    val spawnAlt: Double = 0.0,
    val spawnLabel: String = "World Spawn",
    val createdAt: Long = System.currentTimeMillis()
)

data class GroupMember(
    val userId: String,
    val groupId: String,
    val gamertag: String,
    val skinPreset: String = "steve",
    val customSkinUri: String? = null,
    val lat: Double,
    val lng: Double,
    val alt: Double = 0.0,
    val mcX: Int = 0,
    val mcY: Int = 64,
    val mcZ: Int = 0,
    val heading: Float = 0f,
    val status: String = "Explorando",
    val lastSeen: Long = System.currentTimeMillis()
)

data class UserProfile(
    val id: String,
    val gamertag: String,
    val skinPreset: String = "steve", // steve, alex, diamond_knight, enderman, creeper, miner
    val customSkinUri: String? = null,
    val minecraftUsername: String? = null, // for Minotar/mc-heads skin fetching
    val activeGroupId: String = "default_realm"
)

data class RealmInvite(
    val id: String,
    val realmId: String,
    val realmName: String,
    val realmCode: String,
    val senderGamertag: String,
    val targetGamertag: String,
    val requiresPassword: Boolean = false,
    val realmPassword: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Pendiente"
)
