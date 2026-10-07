package com.example.ui

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.converter.CoordinateConverter
import com.example.data.model.GroupMember
import com.example.data.model.GroupRealm
import com.example.data.model.MinecraftCoords
import com.example.data.model.UserProfile
import com.example.data.model.Waypoint
import com.example.data.model.WaypointType
import com.example.data.repository.CraftMapRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CraftMapRepository(application)
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    // Current real-world user GPS
    val userLat = MutableStateFlow(19.432608)
    val userLng = MutableStateFlow(-99.133209)
    val userAlt = MutableStateFlow(2240.0)
    val userHeading = MutableStateFlow(0f)

    val allRealms: StateFlow<List<GroupRealm>> = repository.allRealms.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val userProfile: StateFlow<UserProfile> = repository.userProfile.combine(userLat) { prof, _ ->
        prof ?: UserProfile(
            id = "user_me",
            gamertag = "AlexCraft99",
            skinPreset = "alex",
            customSkinUri = null,
            minecraftUsername = "Alex",
            activeGroupId = "realm_overworld_main"
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        UserProfile(
            id = "user_me",
            gamertag = "AlexCraft99",
            skinPreset = "alex",
            customSkinUri = null,
            minecraftUsername = "Alex",
            activeGroupId = "realm_overworld_main"
        )
    )

    val activeRealm: StateFlow<GroupRealm?> = combine(allRealms, userProfile) { realms, profile ->
        realms.find { it.id == profile.activeGroupId } ?: realms.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val waypoints: StateFlow<List<Waypoint>> = activeRealm.flatMapLatest { realm ->
        if (realm != null) repository.getWaypoints(realm.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val members: StateFlow<List<GroupMember>> = activeRealm.flatMapLatest { realm ->
        if (realm != null) repository.getMembers(realm.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculated current user Minecraft coordinates
    val currentUserCoords: StateFlow<MinecraftCoords> = combine(
        userLat, userLng, userAlt, activeRealm
    ) { lat, lng, alt, realm ->
        if (realm != null) {
            CoordinateConverter.gpsToMinecraft(
                targetLat = lat,
                targetLng = lng,
                targetAlt = alt,
                spawnLat = realm.spawnLat,
                spawnLng = realm.spawnLng,
                spawnAlt = realm.spawnAlt
            )
        } else {
            MinecraftCoords(0, 64, 0)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MinecraftCoords(0, 64, 0))

    // UI Message feedback
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage = _snackbarMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty(userLat.value, userLng.value)
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun updateGpsLocation(location: Location) {
        userLat.value = location.latitude
        userLng.value = location.longitude
        userAlt.value = location.altitude
        userHeading.value = location.bearing

        // Update member position in current realm
        val realm = activeRealm.value ?: return
        val profile = userProfile.value
        viewModelScope.launch {
            repository.updateMemberLocation(
                groupId = realm.id,
                userId = profile.id,
                gamertag = profile.gamertag,
                skinPreset = profile.skinPreset,
                customSkinUri = profile.customSkinUri,
                lat = location.latitude,
                lng = location.longitude,
                alt = location.altitude,
                spawnLat = realm.spawnLat,
                spawnLng = realm.spawnLng,
                spawnAlt = realm.spawnAlt,
                heading = location.bearing,
                status = "En línea"
            )
        }
    }

    fun fetchCurrentGpsOnce() {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        updateGpsLocation(loc)
                        _snackbarMessage.value = "GPS actualizado con precisión"
                    }
                }
        } catch (e: SecurityException) {
            _snackbarMessage.value = "Permiso de ubicación no concedido"
        }
    }

    fun createRealm(
        name: String,
        description: String,
        useCurrentGpsAsSpawn: Boolean,
        customLat: Double = 0.0,
        customLng: Double = 0.0,
        customAlt: Double = 0.0,
        spawnLabel: String = "World Spawn (0,64,0)"
    ) {
        viewModelScope.launch {
            val spawnLat = if (useCurrentGpsAsSpawn) userLat.value else customLat
            val spawnLng = if (useCurrentGpsAsSpawn) userLng.value else customLng
            val spawnAlt = if (useCurrentGpsAsSpawn) userAlt.value else customAlt

            val newRealm = repository.createRealm(
                name = name,
                description = description,
                spawnLat = spawnLat,
                spawnLng = spawnLng,
                spawnAlt = spawnAlt,
                spawnLabel = spawnLabel
            )

            // Switch to new realm
            val currentProf = userProfile.value
            repository.saveUserProfile(currentProf.copy(activeGroupId = newRealm.id))
            _snackbarMessage.value = "¡Reino '${newRealm.name}' creado con World Spawn!"
        }
    }

    fun joinRealm(code: String) {
        viewModelScope.launch {
            val realm = repository.joinRealmByCode(code)
            if (realm != null) {
                val currentProf = userProfile.value
                repository.saveUserProfile(currentProf.copy(activeGroupId = realm.id))
                _snackbarMessage.value = "Conectado al Reino '${realm.name}'"
            } else {
                _snackbarMessage.value = "Código de Reino no encontrado. Prueba con MC-777"
            }
        }
    }

    fun switchActiveRealm(realmId: String) {
        viewModelScope.launch {
            val currentProf = userProfile.value
            repository.saveUserProfile(currentProf.copy(activeGroupId = realmId))
            _snackbarMessage.value = "Reino activo cambiado"
        }
    }

    fun addWaypoint(
        title: String,
        description: String,
        type: WaypointType,
        targetCoords: MinecraftCoords? = null,
        targetGpsLat: Double? = null,
        targetGpsLng: Double? = null,
        targetGpsAlt: Double? = null
    ) {
        val realm = activeRealm.value ?: return
        viewModelScope.launch {
            val mcCoords: MinecraftCoords
            val lat: Double
            val lng: Double
            val alt: Double

            if (targetCoords != null) {
                mcCoords = targetCoords
                val gps = CoordinateConverter.minecraftToGps(
                    mcCoords = targetCoords,
                    spawnLat = realm.spawnLat,
                    spawnLng = realm.spawnLng,
                    spawnAlt = realm.spawnAlt
                )
                lat = gps.latitude
                lng = gps.longitude
                alt = gps.altitude
            } else if (targetGpsLat != null && targetGpsLng != null) {
                lat = targetGpsLat
                lng = targetGpsLng
                alt = targetGpsAlt ?: realm.spawnAlt
                mcCoords = CoordinateConverter.gpsToMinecraft(
                    targetLat = lat,
                    targetLng = lng,
                    targetAlt = alt,
                    spawnLat = realm.spawnLat,
                    spawnLng = realm.spawnLng,
                    spawnAlt = realm.spawnAlt
                )
            } else {
                lat = userLat.value
                lng = userLng.value
                alt = userAlt.value
                mcCoords = currentUserCoords.value
            }

            val wp = Waypoint(
                id = "wp_" + UUID.randomUUID().toString().take(8),
                groupId = realm.id,
                title = title.ifBlank { type.labelEs },
                description = description,
                type = type,
                gpsLat = lat,
                gpsLng = lng,
                gpsAlt = alt,
                mcX = mcCoords.x,
                mcY = mcCoords.y,
                mcZ = mcCoords.z,
                creatorGamertag = userProfile.value.gamertag
            )
            repository.addWaypoint(wp)
            _snackbarMessage.value = "Pin '${wp.title}' añadido en [X:${wp.mcX}, Z:${wp.mcZ}]"
        }
    }

    fun deleteWaypoint(waypointId: String) {
        val realm = activeRealm.value ?: return
        viewModelScope.launch {
            repository.deleteWaypoint(realm.id, waypointId)
            _snackbarMessage.value = "Pin eliminado"
        }
    }

    fun updateProfile(
        gamertag: String,
        skinPreset: String,
        customSkinUri: String?,
        minecraftUsername: String?
    ) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(
                gamertag = gamertag.ifBlank { "Alex" },
                skinPreset = skinPreset,
                customSkinUri = customSkinUri,
                minecraftUsername = minecraftUsername
            )
            repository.saveUserProfile(updated)

            // Update member entry
            val realm = activeRealm.value
            if (realm != null) {
                repository.updateMemberLocation(
                    groupId = realm.id,
                    userId = updated.id,
                    gamertag = updated.gamertag,
                    skinPreset = updated.skinPreset,
                    customSkinUri = updated.customSkinUri,
                    lat = userLat.value,
                    lng = userLng.value,
                    alt = userAlt.value,
                    spawnLat = realm.spawnLat,
                    spawnLng = realm.spawnLng,
                    spawnAlt = realm.spawnAlt
                )
            }
            _snackbarMessage.value = "Perfil de jugador actualizado"
        }
    }

    fun removeCustomSkin() {
        viewModelScope.launch {
            val updated = userProfile.value.copy(customSkinUri = null)
            repository.saveUserProfile(updated)
            _snackbarMessage.value = "Skin personalizada eliminada"
        }
    }

    fun setSimulationLocationOffset(dLat: Double, dLng: Double) {
        userLat.value += dLat
        userLng.value += dLng
        val realm = activeRealm.value ?: return
        val profile = userProfile.value
        viewModelScope.launch {
            repository.updateMemberLocation(
                groupId = realm.id,
                userId = profile.id,
                gamertag = profile.gamertag,
                skinPreset = profile.skinPreset,
                customSkinUri = profile.customSkinUri,
                lat = userLat.value,
                lng = userLng.value,
                alt = userAlt.value,
                spawnLat = realm.spawnLat,
                spawnLng = realm.spawnLng,
                spawnAlt = realm.spawnAlt,
                status = "Caminando"
            )
        }
    }
}
