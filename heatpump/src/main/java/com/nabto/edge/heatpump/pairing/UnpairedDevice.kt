package com.nabto.edge.heatpump.pairing

interface UnpairedDevice {

    public data class ClientSettings(val serverUrl : String, val serverKey : String);

    suspend fun connect();
    suspend fun coapPairing();
    suspend fun getClientSettings() : ClientSettings;
    fun getDeviceFingerprintHex() : String;
}