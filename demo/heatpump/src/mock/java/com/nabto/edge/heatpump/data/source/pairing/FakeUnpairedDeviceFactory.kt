package com.nabto.edge.heatpump.data.source.pairing

import com.nabto.edge.heatpump.pairing.UnpairedDevice
import com.nabto.edge.heatpump.pairing.UnpairedDeviceFactory

class FakeUnpairedDeviceFactory : UnpairedDeviceFactory {
    override fun createUnpairedDevice(productId: String, deviceId: String): UnpairedDevice {
        return FakeUnpairedDeviceImpl()
    }

    override fun createUnPairedDevice(productId: String, deviceId: String, serverUrl: String, serverKey: String): UnpairedDevice {
        return FakeUnpairedDeviceImpl()
    }
}