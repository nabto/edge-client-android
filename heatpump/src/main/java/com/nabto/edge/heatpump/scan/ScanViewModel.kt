package com.nabto.edge.heatpump.scan;

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.nabto.edge.heatpump.DeviceListItem
import com.nabto.edge.heatpump.data.source.LocalDevicesRepository
import javax.inject.Inject

class ScanViewModel @Inject constructor(val localDevicesRepository: LocalDevicesRepository) : ViewModel() {

    fun getDiscoveredDevices() : LiveData<List<DeviceListItem>> {
        return localDevicesRepository.getLocalDevices()
    }
}
