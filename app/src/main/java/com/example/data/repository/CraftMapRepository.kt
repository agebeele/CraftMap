package com.example.data.repository

import android.content.Context
import com.example.data.converter.CoordinateConverter
import com.example.data.local.AppDatabase
import com.example.data.local.GroupMemberEntity
import com.example.data.local.GroupRealmEntity
import com.example.data.local.UserProfileEntity
import com.example.data.local.WaypointEntity
import com.example.data.model.GroupMember
import com.example.data.model.GroupRealm
import com.example.data.model.UserProfile
import com.example.data.model.Waypoint
import com.example.data.model.WaypointType
import com.example.data.remote.FirebaseSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class CraftMapRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val waypointDao = db.waypointDao()
    private val realmDao = db.realmDao()
    private val memberDao = db.memberDao()
    private val userDao = db.userDao()
    private val firebaseSync = FirebaseSyncManager(context)
    private val repoScope = CoroutineScope(Dispatchers.IO)

    val allRealms: Flow<List<GroupRealm>> = realmDao.getAllRealms().map { entities ->
        entities.map { it.toDomain() }
    }

    val userProfile: Flow<UserProfile?> = userDao.getUserProfile().map { it?.toDomain() }

    fun getRealmById(realmId: String): Flow<GroupRealm?> = realmDao.getRealmById(realmId).map { it?.toDomain() }

    fun getWaypoints(groupId: String): Flow<List<Waypoint>> = waypointDao.getWaypointsForGroup(groupId).map { list ->
        list.map { it.toDomain() }
    }

    fun getMembers(groupId: String): Flow<List<GroupMember>> = memberDao.getMembersForGroup(groupId).map { list ->
        list.map { it.toDomain() }
    }

    suspend fun initializeDefaultDataIfEmpty(currentLat: Double = 19.432608, currentLng: Double = -99.133209) {
        val existingProfile = userDao.getUserProfile().firstOrNull()
        if (existingProfile == null) {
            val defaultRealmId = "realm_overworld_main"
            val defaultRealm = GroupRealm(
                id = defaultRealmId,
                name = "Servidor Overworld Alpha",
                code = "MC-777",
                description = "Mundo principal de amigos con World Spawn calibrado en (0, 64, 0)",
                spawnLat = currentLat,
                spawnLng = currentLng,
                spawnAlt = 2240.0,
                spawnLabel = "World Spawn Beacon (0,64,0)"
            )
            realmDao.insertRealm(GroupRealmEntity.fromDomain(defaultRealm))

            val profile = UserProfile(
                id = "user_me",
                gamertag = "AlexCraft99",
                skinPreset = "alex",
                customSkinUri = null,
                minecraftUsername = "Alex",
                activeGroupId = defaultRealmId
            )
            userDao.saveUserProfile(UserProfileEntity.fromDomain(profile))

            // Add companion friends with Minecraft skins & live coordinates
            val friend1 = GroupMember(
                userId = "friend_steve",
                groupId = defaultRealmId,
                gamertag = "SteveTheMiner",
                skinPreset = "steve",
                lat = currentLat + 0.0012,
                lng = currentLng + 0.0018,
                alt = 2242.0,
                mcX = 189,
                mcY = 66,
                mcZ = -133,
                heading = 45f,
                status = "Picando diamantes"
            )
            val friend2 = GroupMember(
                userId = "friend_diamond",
                groupId = defaultRealmId,
                gamertag = "Knight_Creeper",
                skinPreset = "diamond_knight",
                lat = currentLat - 0.0015,
                lng = currentLng - 0.0009,
                alt = 2235.0,
                mcX = -94,
                mcY = 59,
                mcZ = 167,
                heading = 190f,
                status = "Construyendo base"
            )
            val friend3 = GroupMember(
                userId = "friend_ender",
                groupId = defaultRealmId,
                gamertag = "EnderBoy_42",
                skinPreset = "enderman",
                lat = currentLat + 0.0025,
                lng = currentLng - 0.0015,
                alt = 2250.0,
                mcX = -157,
                mcY = 74,
                mcZ = -278,
                heading = 310f,
                status = "Buscando portal"
            )
            memberDao.insertMembers(listOf(
                GroupMemberEntity.fromDomain(friend1),
                GroupMemberEntity.fromDomain(friend2),
                GroupMemberEntity.fromDomain(friend3)
            ))

            // Initial Waypoints
            val wpSpawn = Waypoint(
                id = "wp_spawn_origin",
                groupId = defaultRealmId,
                title = "World Spawn Beacon",
                description = "Punto central cero del mundo (X:0, Y:64, Z:0)",
                type = WaypointType.SPAWN,
                gpsLat = currentLat,
                gpsLng = currentLng,
                gpsAlt = 2240.0,
                mcX = 0,
                mcY = 64,
                mcZ = 0,
                creatorGamertag = "Servidor"
            )
            val wpBase = Waypoint(
                id = "wp_home_base",
                groupId = defaultRealmId,
                title = "Base Principal y Cofres",
                description = "Nuestra fortaleza comunitaria y almacén de minerales",
                type = WaypointType.HOME,
                gpsLat = currentLat + 0.0008,
                gpsLng = currentLng + 0.0015,
                gpsAlt = 2245.0,
                mcX = 157,
                mcY = 69,
                mcZ = -89,
                creatorGamertag = "AlexCraft99"
            )
            val wpNether = Waypoint(
                id = "wp_nether_portal",
                groupId = defaultRealmId,
                title = "Portal a la Dimensión Nether",
                description = "Portal de obsidiana rápida conexión",
                type = WaypointType.NETHER_PORTAL,
                gpsLat = currentLat - 0.0020,
                gpsLng = currentLng + 0.0022,
                gpsAlt = 2238.0,
                mcX = 230,
                mcY = 62,
                mcZ = 222,
                creatorGamertag = "SteveTheMiner"
            )
            val wpMine = Waypoint(
                id = "wp_deepslate_mine",
                groupId = defaultRealmId,
                title = "Mina de Diamantes Y=-58",
                description = "Túnel profundo directo a la capa de diamantes y redstone",
                type = WaypointType.MINE,
                gpsLat = currentLat - 0.0011,
                gpsLng = currentLng - 0.0020,
                gpsAlt = 2118.0,
                mcX = -210,
                mcY = -58,
                mcZ = 122,
                creatorGamertag = "Knight_Creeper"
            )

            waypointDao.insertWaypoints(listOf(
                WaypointEntity.fromDomain(wpSpawn),
                WaypointEntity.fromDomain(wpBase),
                WaypointEntity.fromDomain(wpNether),
                WaypointEntity.fromDomain(wpMine)
            ))

            // Sync to Firebase if available
            repoScope.launch {
                firebaseSync.syncRealm(defaultRealm)
                firebaseSync.syncWaypoint(wpSpawn)
                firebaseSync.syncWaypoint(wpBase)
                firebaseSync.syncWaypoint(wpNether)
                firebaseSync.syncWaypoint(wpMine)
            }
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        userDao.saveUserProfile(UserProfileEntity.fromDomain(profile))
    }

    suspend fun createRealm(
        name: String,
        description: String,
        spawnLat: Double,
        spawnLng: Double,
        spawnAlt: Double,
        password: String = "",
        spawnLabel: String = "World Spawn"
    ): GroupRealm {
        val randomCode = "MC-" + (100..999).random()
        val realm = GroupRealm(
            id = "realm_" + UUID.randomUUID().toString().take(8),
            name = name,
            code = randomCode,
            description = description,
            password = password,
            spawnLat = spawnLat,
            spawnLng = spawnLng,
            spawnAlt = spawnAlt,
            spawnLabel = spawnLabel
        )
        realmDao.insertRealm(GroupRealmEntity.fromDomain(realm))

        // Auto-create World Spawn Waypoint
        val spawnWp = Waypoint(
            id = "wp_spawn_" + UUID.randomUUID().toString().take(8),
            groupId = realm.id,
            title = spawnLabel,
            description = "Punto de Respawn Oficial (X: 0, Y: 64, Z: 0)",
            type = WaypointType.SPAWN,
            gpsLat = spawnLat,
            gpsLng = spawnLng,
            gpsAlt = spawnAlt,
            mcX = 0,
            mcY = 64,
            mcZ = 0,
            creatorGamertag = "Host"
        )
        waypointDao.insertWaypoint(WaypointEntity.fromDomain(spawnWp))

        repoScope.launch {
            firebaseSync.syncRealm(realm)
            firebaseSync.syncWaypoint(spawnWp)
        }
        return realm
    }

    suspend fun joinRealmByCode(code: String): GroupRealm? {
        return realmDao.getRealmByCode(code.trim().uppercase())?.toDomain()
    }

    suspend fun addWaypoint(waypoint: Waypoint) {
        waypointDao.insertWaypoint(WaypointEntity.fromDomain(waypoint))
        repoScope.launch {
            firebaseSync.syncWaypoint(waypoint)
        }
    }

    suspend fun deleteWaypoint(groupId: String, waypointId: String) {
        waypointDao.deleteWaypoint(waypointId)
        repoScope.launch {
            firebaseSync.deleteWaypoint(groupId, waypointId)
        }
    }

    suspend fun updateMemberLocation(
        groupId: String,
        userId: String,
        gamertag: String,
        skinPreset: String,
        customSkinUri: String?,
        lat: Double,
        lng: Double,
        alt: Double,
        spawnLat: Double,
        spawnLng: Double,
        spawnAlt: Double,
        heading: Float = 0f,
        status: String = "Online"
    ) {
        val coords = CoordinateConverter.gpsToMinecraft(
            targetLat = lat,
            targetLng = lng,
            targetAlt = alt,
            spawnLat = spawnLat,
            spawnLng = spawnLng,
            spawnAlt = spawnAlt
        )

        val member = GroupMember(
            userId = userId,
            groupId = groupId,
            gamertag = gamertag,
            skinPreset = skinPreset,
            customSkinUri = customSkinUri,
            lat = lat,
            lng = lng,
            alt = alt,
            mcX = coords.x,
            mcY = coords.y,
            mcZ = coords.z,
            heading = heading,
            status = status,
            lastSeen = System.currentTimeMillis()
        )
        memberDao.insertOrUpdateMember(GroupMemberEntity.fromDomain(member))

        repoScope.launch {
            firebaseSync.updateMemberLocation(member)
        }
    }
}
