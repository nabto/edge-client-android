package com.nabto.edge.heatpump.pairing

import com.nabto.edge.client.Connection

interface UnpairedDevice {

    public data class ClientSettings(val serverUrl : String, val serverKey : String);

    suspend fun connect();
    fun getConnection() : Connection;
    fun getDeviceFingerprint() : String;
}