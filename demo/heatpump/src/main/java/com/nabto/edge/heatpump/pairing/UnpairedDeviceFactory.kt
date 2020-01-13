package com.nabto.edge.heatpump.pairing

interface UnpairedDeviceFactory {
    fun createUnpairedDevice(productId : String, deviceId : String) : UnpairedDevice;
    fun createUnPairedDevice(productId : String, deviceId : String, serverUrl : String, serverKey: String) : UnpairedDevice
}