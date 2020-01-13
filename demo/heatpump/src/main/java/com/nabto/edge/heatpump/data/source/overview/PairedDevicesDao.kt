package com.nabto.edge.heatpump.data.source.overview

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nabto.edge.heatpump.data.source.overview.PairedDevice

@Dao
interface PairedDevicesDao {
    @Query("SELECT * from paired_devices ORDER BY device_id ASC")
    fun getPairedDevices(): LiveData<List<PairedDevice>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(pairedDevice: PairedDevice)

    @Query("DELETE FROM paired_devices")
    suspend fun deleteAll()

    @Query ("SELECT * from paired_devices WHERE product_id = :productId AND device_id = :deviceId")
    suspend fun getPairedDevice(productId : String, deviceId : String) : PairedDevice
}