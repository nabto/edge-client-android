package com.nabto.edge.heatpump.data.source

import androidx.lifecycle.LiveData
import com.nabto.edge.heatpump.DeviceListItem

interface LocalDevicesRepository {
    fun getLocalDevices() : LiveData<List<DeviceListItem>>
}