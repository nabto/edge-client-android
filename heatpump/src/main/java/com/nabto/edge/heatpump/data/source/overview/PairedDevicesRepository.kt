package com.nabto.edge.heatpump.data.source.overview

import androidx.lifecycle.LiveData
import com.nabto.edge.heatpump.data.source.overview.PairedDevice
import com.nabto.edge.heatpump.data.source.overview.PairedDevicesDao

class PairedDevicesRepository(private val pairedDevicesDao: PairedDevicesDao) {
    val allPairedDevices: LiveData<List<PairedDevice>> = pairedDevicesDao.getPairedDevices()

    suspend fun insert(pairedDevice: PairedDevice) {
        pairedDevicesDao.insert(pairedDevice)
    }
}