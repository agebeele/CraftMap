package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WaypointDao {
    @Query("SELECT * FROM waypoints WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun getWaypointsForGroup(groupId: String): Flow<List<WaypointEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaypoint(waypoint: WaypointEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaypoints(waypoints: List<WaypointEntity>)

    @Query("DELETE FROM waypoints WHERE id = :id")
    suspend fun deleteWaypoint(id: String)

    @Query("DELETE FROM waypoints WHERE groupId = :groupId")
    suspend fun deleteAllInGroup(groupId: String)
}

@Dao
interface RealmDao {
    @Query("SELECT * FROM realms ORDER BY createdAt DESC")
    fun getAllRealms(): Flow<List<GroupRealmEntity>>

    @Query("SELECT * FROM realms WHERE id = :id LIMIT 1")
    fun getRealmById(id: String): Flow<GroupRealmEntity?>

    @Query("SELECT * FROM realms WHERE code = :code LIMIT 1")
    suspend fun getRealmByCode(code: String): GroupRealmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRealm(realm: GroupRealmEntity)

    @Query("DELETE FROM realms WHERE id = :id")
    suspend fun deleteRealm(id: String)
}

@Dao
interface MemberDao {
    @Query("SELECT * FROM group_members WHERE groupId = :groupId ORDER BY lastSeen DESC")
    fun getMembersForGroup(groupId: String): Flow<List<GroupMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMember(member: GroupMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<GroupMemberEntity>)

    @Query("DELETE FROM group_members WHERE userId = :userId AND groupId = :groupId")
    suspend fun removeMember(userId: String, groupId: String)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)
}
