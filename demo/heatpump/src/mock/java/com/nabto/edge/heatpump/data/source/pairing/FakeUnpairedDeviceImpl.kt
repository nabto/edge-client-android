package com.nabto.edge.heatpump.data.source.pairing

import com.nabto.edge.heatpump.pairing.UnpairedDevice

class FakeUnpairedDeviceImpl : UnpairedDevice {
    override suspend fun connect() {
        return;
    }

    override suspend fun coapPairing() {
        return;
    }

    override suspend fun getClientSettings(): UnpairedDevice.ClientSettings {
        return UnpairedDevice.ClientSettings("https://foo.bar.baz", "123123")
    }

    override fun getDeviceFingerprintHex(): String {
        return "12341234123412341234123412341234"
    }

}