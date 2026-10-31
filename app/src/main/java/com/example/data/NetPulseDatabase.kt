package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest

@Dao
interface NetPulseDao {
    @Query("SELECT * FROM network_devices ORDER BY isOnline DESC, isGateway DESC, latencyMs ASC")
    fun observeAllDevices(): Flow<List<NetworkDeviceEntity>>

    @Query("SELECT * FROM network_devices")
    suspend fun getAllDevicesSnapshot(): List<NetworkDeviceEntity>

    @Query("SELECT * FROM network_devices WHERE macAddress = :mac LIMIT 1")
    suspend fun getDeviceByMac(mac: String): NetworkDeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDevices(devices: List<NetworkDeviceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDevice(device: NetworkDeviceEntity)

    @Query("UPDATE network_devices SET trustStatus = :trustStatus, securityNotes = :notes WHERE macAddress = :mac")
    suspend fun updateDeviceTrust(mac: String, trustStatus: String, notes: String)

    @Query("UPDATE network_devices SET customAlias = :alias, roomLocation = :room, securityNotes = :notes WHERE macAddress = :mac")
    suspend fun updateDeviceIdentity(mac: String, alias: String, room: String, notes: String)

    @Query("UPDATE network_devices SET isWatchdogEnabled = :enabled WHERE macAddress = :mac")
    suspend fun updateDeviceWatchdog(mac: String, enabled: Boolean)

    @Query("SELECT * FROM connection_events ORDER BY timestampEpochMs DESC LIMIT 250")
    fun observeConnectionEvents(): Flow<List<ConnectionEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnectionEvent(event: ConnectionEventEntity)

    @Query("DELETE FROM connection_events")
    suspend fun clearAllConnectionEvents()

    @Query("SELECT * FROM signal_heatmap_points ORDER BY timestampEpochMs DESC")
    fun observeSignalHeatmapPoints(): Flow<List<SignalHeatmapPointEntity>>

    @Query("SELECT COUNT(*) FROM signal_heatmap_points")
    suspend fun getSignalHeatmapCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignalHeatmapPoint(point: SignalHeatmapPointEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignalHeatmapPoints(points: List<SignalHeatmapPointEntity>)

    @Query("DELETE FROM signal_heatmap_points WHERE id = :id")
    suspend fun deleteSignalHeatmapPoint(id: Long)
}

@Database(
    entities = [
        NetworkDeviceEntity::class,
        ConnectionEventEntity::class,
        SignalHeatmapPointEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class NetPulseDatabase : RoomDatabase() {
    abstract fun netPulseDao(): NetPulseDao

    companion object {
        @Volatile
        private var INSTANCE: NetPulseDatabase? = null

        fun getDatabase(context: Context): NetPulseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NetPulseDatabase::class.java,
                    "netpulse_pro_secure.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }

        fun computeSha256Signature(payload: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(payload.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }.take(24).uppercase()
        }
    }
}
