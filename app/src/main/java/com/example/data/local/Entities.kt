package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.GroupMember
import com.example.data.model.GroupRealm
import com.example.data.model.UserProfile
import com.example.data.model.Waypoint
import com.example.data.model.WaypointType

@Entity(tableName = "waypoints")
data class WaypointEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val title: String,
    val description: String,
    val type: String,
    val gpsLat: Double,
    val gpsLng: Double,
    val gpsAlt: Double,
    val mcX: Int,
    val mcY: Int,
    val mcZ: Int,
    val creatorGamertag: String,
    val createdAt: Long
) {
    fun toDomain(): Waypoint = Waypoint(
        id = id,
        groupId = groupId,
        title = title,
        description = description,
        type = runCatching { WaypointType.valueOf(type) }.getOrDefault(WaypointType.CUSTOM),
        gpsLat = gpsLat,
        gpsLng = gpsLng,
        gpsAlt = gpsAlt,
        mcX = mcX,
        mcY = mcY,
        mcZ = mcZ,
        creatorGamertag = creatorGamertag,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(w: Waypoint): WaypointEntity = WaypointEntity(
            id = w.id,
            groupId = w.groupId,
            title = w.title,
            description = w.description,
            type = w.type.name,
            gpsLat = w.gpsLat,
            gpsLng = w.gpsLng,
            gpsAlt = w.gpsAlt,
            mcX = w.mcX,
            mcY = w.mcY,
            mcZ = w.mcZ,
            creatorGamertag = w.creatorGamertag,
            createdAt = w.createdAt
        )
    }
}

@Entity(tableName = "realms")
data class GroupRealmEntity(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val description: String,
    val password: String = "",
    val spawnLat: Double,
    val spawnLng: Double,
    val spawnAlt: Double,
    val spawnLabel: String,
    val createdAt: Long
) {
    fun toDomain(): GroupRealm = GroupRealm(
        id = id,
        name = name,
        code = code,
        description = description,
        password = password,
        spawnLat = spawnLat,
        spawnLng = spawnLng,
        spawnAlt = spawnAlt,
        spawnLabel = spawnLabel,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(g: GroupRealm): GroupRealmEntity = GroupRealmEntity(
            id = g.id,
            name = g.name,
            code = g.code,
            description = g.description,
            password = g.password,
            spawnLat = g.spawnLat,
            spawnLng = g.spawnLng,
            spawnAlt = g.spawnAlt,
            spawnLabel = g.spawnLabel,
            createdAt = g.createdAt
        )
    }
}

@Entity(tableName = "group_members")
data class GroupMemberEntity(
    @PrimaryKey val userId: String,
    val groupId: String,
    val gamertag: String,
    val skinPreset: String,
    val customSkinUri: String?,
    val lat: Double,
    val lng: Double,
    val alt: Double,
    val mcX: Int,
    val mcY: Int,
    val mcZ: Int,
    val heading: Float,
    val status: String,
    val lastSeen: Long
) {
    fun toDomain(): GroupMember = GroupMember(
        userId = userId,
        groupId = groupId,
        gamertag = gamertag,
        skinPreset = skinPreset,
        customSkinUri = customSkinUri,
        lat = lat,
        lng = lng,
        alt = alt,
        mcX = mcX,
        mcY = mcY,
        mcZ = mcZ,
        heading = heading,
        status = status,
        lastSeen = lastSeen
    )

    companion object {
        fun fromDomain(m: GroupMember): GroupMemberEntity = GroupMemberEntity(
            userId = m.userId,
            groupId = m.groupId,
            gamertag = m.gamertag,
            skinPreset = m.skinPreset,
            customSkinUri = m.customSkinUri,
            lat = m.lat,
            lng = m.lng,
            alt = m.alt,
            mcX = m.mcX,
            mcY = m.mcY,
            mcZ = m.mcZ,
            heading = m.heading,
            status = m.status,
            lastSeen = m.lastSeen
        )
    }
}

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val gamertag: String,
    val skinPreset: String,
    val customSkinUri: String?,
    val minecraftUsername: String?,
    val activeGroupId: String
) {
    fun toDomain(): UserProfile = UserProfile(
        id = id,
        gamertag = gamertag,
        skinPreset = skinPreset,
        customSkinUri = customSkinUri,
        minecraftUsername = minecraftUsername,
        activeGroupId = activeGroupId
    )

    companion object {
        fun fromDomain(p: UserProfile): UserProfileEntity = UserProfileEntity(
            id = p.id,
            gamertag = p.gamertag,
            skinPreset = p.skinPreset,
            customSkinUri = p.customSkinUri,
            minecraftUsername = p.minecraftUsername,
            activeGroupId = p.activeGroupId
        )
    }
}
