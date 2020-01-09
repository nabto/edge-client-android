package com.nabto.edge.heatpump.data.source

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nabto.edge.heatpump.DeviceListItem

class FakeLocalDevices : LocalDevicesRepository {
    override fun getLocalDevices() : LiveData<List<DeviceListItem>> {
        return MutableLiveData(listOf(FakeDevice("pr-12345678", "de-13371337", "test device 1"), FakeDevice("pr-12345678", "de-abcabcab", "test device 2")))
    }
}