package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.data.model.GroupMember
import com.example.data.model.GroupRealm
import com.example.data.model.Waypoint
import com.example.data.model.WaypointType
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseSyncManager(private val context: Context) {
    private val TAG = "CraftMapFirebase"

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                Log.d(TAG, "FirebaseApp is not initialized yet. Using local mode.")
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Firestore unavailable: ${e.message}")
            null
        }
    }

    val isFirebaseAvailable: Boolean
        get() = firestore != null

    suspend fun syncRealm(realm: GroupRealm) {
        val db = firestore ?: return
        try {
            val doc = db.collection("realms").document(realm.id)
            val data = hashMapOf(
                "id" to realm.id,
                "name" to realm.name,
                "code" to realm.code,
                "description" to realm.description,
                "spawnLat" to realm.spawnLat,
                "spawnLng" to realm.spawnLng,
                "spawnAlt" to realm.spawnAlt,
                "spawnLabel" to realm.spawnLabel,
                "createdAt" to realm.createdAt
            )
            doc.set(data).await()
            Log.d(TAG, "Synced realm ${realm.name} to Firestore")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync realm to Firestore: ${e.message}")
        }
    }

    suspend fun syncWaypoint(waypoint: Waypoint) {
        val db = firestore ?: return
        try {
            val doc = db.collection("realms")
                .document(waypoint.groupId)
                .collection("waypoints")
                .document(waypoint.id)

            val data = hashMapOf(
                "id" to waypoint.id,
                "groupId" to waypoint.groupId,
                "title" to waypoint.title,
                "description" to waypoint.description,
                "type" to waypoint.type.name,
                "gpsLat" to waypoint.gpsLat,
                "gpsLng" to waypoint.gpsLng,
                "gpsAlt" to waypoint.gpsAlt,
                "mcX" to waypoint.mcX,
                "mcY" to waypoint.mcY,
                "mcZ" to waypoint.mcZ,
                "creatorGamertag" to waypoint.creatorGamertag,
                "createdAt" to waypoint.createdAt
            )
            doc.set(data).await()
            Log.d(TAG, "Synced waypoint ${waypoint.title} to Firestore")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync waypoint to Firestore: ${e.message}")
        }
    }

    suspend fun deleteWaypoint(groupId: String, waypointId: String) {
        val db = firestore ?: return
        try {
            db.collection("realms")
                .document(groupId)
                .collection("waypoints")
                .document(waypointId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete waypoint in Firestore: ${e.message}")
        }
    }

    suspend fun updateMemberLocation(member: GroupMember) {
        val db = firestore ?: return
        try {
            val doc = db.collection("realms")
                .document(member.groupId)
                .collection("members")
                .document(member.userId)

            val data = hashMapOf(
                "userId" to member.userId,
                "groupId" to member.groupId,
                "gamertag" to member.gamertag,
                "skinPreset" to member.skinPreset,
                "customSkinUri" to (member.customSkinUri ?: ""),
                "lat" to member.lat,
                "lng" to member.lng,
                "alt" to member.alt,
                "mcX" to member.mcX,
                "mcY" to member.mcY,
                "mcZ" to member.mcZ,
                "heading" to member.heading,
                "status" to member.status,
                "lastSeen" to member.lastSeen
            )
            doc.set(data).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update member in Firestore: ${e.message}")
        }
    }

    fun observeRemoteWaypoints(groupId: String): Flow<List<Waypoint>> = callbackFlow {
        val db = firestore
        if (db == null) {
            close()
            return@callbackFlow
        }

        var registration: ListenerRegistration? = null
        try {
            registration = db.collection("realms")
                .document(groupId)
                .collection("waypoints")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Firestore waypoints listener error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val waypoints = snapshot.documents.mapNotNull { doc ->
                            try {
                                Waypoint(
                                    id = doc.getString("id") ?: doc.id,
                                    groupId = doc.getString("groupId") ?: groupId,
                                    title = doc.getString("title") ?: "Sin título",
                                    description = doc.getString("description") ?: "",
                                    type = runCatching {
                                        WaypointType.valueOf(doc.getString("type") ?: "CUSTOM")
                                    }.getOrDefault(WaypointType.CUSTOM),
                                    gpsLat = doc.getDouble("gpsLat") ?: 0.0,
                                    gpsLng = doc.getDouble("gpsLng") ?: 0.0,
                                    gpsAlt = doc.getDouble("gpsAlt") ?: 0.0,
                                    mcX = (doc.getLong("mcX") ?: 0L).toInt(),
                                    mcY = (doc.getLong("mcY") ?: 64L).toInt(),
                                    mcZ = (doc.getLong("mcZ") ?: 0L).toInt(),
                                    creatorGamertag = doc.getString("creatorGamertag") ?: "Compañero",
                                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(waypoints)
                    }
                }
        } catch (e: Exception) {
            close(e)
        }

        awaitClose {
            registration?.remove()
        }
    }

    fun observeRemoteMembers(groupId: String): Flow<List<GroupMember>> = callbackFlow {
        val db = firestore
        if (db == null) {
            close()
            return@callbackFlow
        }

        var registration: ListenerRegistration? = null
        try {
            registration = db.collection("realms")
                .document(groupId)
                .collection("members")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Firestore members listener error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val members = snapshot.documents.mapNotNull { doc ->
                            try {
                                GroupMember(
                                    userId = doc.getString("userId") ?: doc.id,
                                    groupId = doc.getString("groupId") ?: groupId,
                                    gamertag = doc.getString("gamertag") ?: "Steve",
                                    skinPreset = doc.getString("skinPreset") ?: "steve",
                                    customSkinUri = doc.getString("customSkinUri")?.takeIf { it.isNotBlank() },
                                    lat = doc.getDouble("lat") ?: 0.0,
                                    lng = doc.getDouble("lng") ?: 0.0,
                                    alt = doc.getDouble("alt") ?: 0.0,
                                    mcX = (doc.getLong("mcX") ?: 0L).toInt(),
                                    mcY = (doc.getLong("mcY") ?: 64L).toInt(),
                                    mcZ = (doc.getLong("mcZ") ?: 0L).toInt(),
                                    heading = (doc.getDouble("heading") ?: 0.0).toFloat(),
                                    status = doc.getString("status") ?: "Online",
                                    lastSeen = doc.getLong("lastSeen") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(members)
                    }
                }
        } catch (e: Exception) {
            close(e)
        }

        awaitClose {
            registration?.remove()
        }
    }
}
