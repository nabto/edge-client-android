package com.nabto.edge.heatpump.pairing

import android.content.SharedPreferences
import com.fasterxml.jackson.annotation.JsonProperty
import com.nabto.edge.client.Connection
import com.nabto.edge.client.NabtoClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class UnpairedDeviceImpl : UnpairedDevice {

    private var options  = JSONObject();

    lateinit private var sharedPreferences : SharedPreferences;
    lateinit private var nabtoClient : NabtoClient

    constructor(productId : String, deviceId : String, sharedPreferences: SharedPreferences, nabtoClient: NabtoClient) {
        options.put("ProductId", productId);
        options.put("DeviceId", deviceId);
        options.put("Remote", false);
        this.sharedPreferences = sharedPreferences
        this.nabtoClient = nabtoClient
    }

    constructor(productId: String, deviceId: String, serverUrl : String, serverKey : String, sharedPreferences: SharedPreferences, nabtoClient: NabtoClient)
    {
        options.put("ProductId", productId);
        options.put("DeviceId", deviceId);
        options.put("ServerUrl", serverUrl);
        options.put("ServerKey", serverKey);
        this.sharedPreferences = sharedPreferences
        this.nabtoClient = nabtoClient
    }

    private class IAMUser {
        @JsonProperty("Username", required = true) var username : String = ""
        @JsonProperty("Role", required = false) var role : String = ""
        @JsonProperty("Fingerprint", required = false) var fingerprint : String = ""
        @JsonProperty("DisplayName", required = false) var displayName : String = ""
        @JsonProperty("Sct", required = false) var sct : String = ""
    }

    private class IAMPairing {
        @JsonProperty("Modes", required = false) var modes : List<String> = listOf()
        @JsonProperty("NabtoVersion", required = false) var nabtoVersion : String = ""
        @JsonProperty("AppVersion", required = false) var appVersion : String = ""
        @JsonProperty("AppName", required = false) var appName : String = ""
        @JsonProperty("ProductId", required = false) var productId : String = ""
        @JsonProperty("DeviceId", required = false) var deviceId : String = ""
    }

    private fun createConnection() : Connection {
        val c = nabtoClient.createConnection();
        options.put("PrivateKey", getPrivateKey())
        c.updateOptions(options.toString())
        return c;
    }

    val conn : Connection by lazy {
        createConnection();
    }

    override public fun getConnection() : Connection {
        return conn;
    }

    fun getPrivateKey() : String {
        if (!sharedPreferences.contains("nabto_private_key")) {
            sharedPreferences.edit().putString("nabto_private_key", nabtoClient.createPrivateKey()).commit()
        }
        return sharedPreferences.getString("nabto_private_key", "")!!
    }

    override suspend fun connect() {
        conn.connect();
    }

    override fun getDeviceFingerprint(): String {
        return conn.deviceFingerprint;
    }
}