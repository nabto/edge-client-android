package com.nabto.edge.heatpump.pairing

import android.content.SharedPreferences
import com.nabto.edge.client.NabtoClient
import javax.inject.Inject

class UnpairedDeviceFactoryImpl @Inject constructor() : UnpairedDeviceFactory {
    @Inject lateinit var nabtoClient: NabtoClient
    @Inject lateinit var sharedPreferences: SharedPreferences
    override fun createUnpairedDevice(productId: String, deviceId: String): UnpairedDevice {
        return UnpairedDeviceImpl(productId, deviceId, sharedPreferences, nabtoClient)
    }

    override fun createUnPairedDevice(productId: String, deviceId: String, serverUrl: String, serverKey: String): UnpairedDevice {
        return UnpairedDeviceImpl(productId, deviceId, serverUrl, serverKey, sharedPreferences, nabtoClient);
    }
}