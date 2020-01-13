package com.nabto.edge.heatpump.pairing

import android.content.SharedPreferences
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.nabto.edge.client.Connection
import com.nabto.edge.client.NabtoClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

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

    private class ClientSettingsCoap {
        @JsonProperty("ServerUrl", required = true) var serverUrl : String = ""
        @JsonProperty("ServerKey", required = true) var serverKey : String = ""
    }

    private fun createConnection() : Connection {
        val c = nabtoClient.createConnection();
        options.put("PrivateKey", getPrivateKey())
        c.updateOptions(options.toString())
        return c;
    }

    val connection : Connection by lazy {
        createConnection();
    }

    fun getPrivateKey() : String {
        if (!sharedPreferences.contains("nabto_private_key")) {
            sharedPreferences.edit().putString("nabto_private_key", nabtoClient.createPrivateKey()).commit()
        }
        return sharedPreferences.getString("nabto_private_key", "")!!
    }

    override suspend fun connect() {
        connection.connect();
    }

    override suspend fun coapPairing() {
        withContext(Dispatchers.IO) {
            val coap = connection.createCoap("POST", "/pairing/button")
            coap.execute()
            if (coap.responseStatusCode != 201) {
                throw(Exception("invalid coap response code " + coap.responseStatusCode));
                return@withContext;
            }
        }
    }

    override suspend fun getClientSettings() : UnpairedDevice.ClientSettings {
        return withContext(Dispatchers.IO) {
            val csCoap = connection.createCoap("GET", "/beta/client-settings")
            csCoap.execute()
            if (csCoap.responseStatusCode != 205) {
                throw(Exception("Invalid coap client settings response " + csCoap.responseStatusCode));
            }
            val f = CBORFactory();
            val mapper = ObjectMapper(f);
            val clientSettings = mapper.readValue<ClientSettingsCoap>(csCoap.responsePayload, ClientSettingsCoap::class.java)
            return@withContext UnpairedDevice.ClientSettings( clientSettings.serverUrl, clientSettings.serverKey);
        }
    }

    override fun getDeviceFingerprintHex(): String {
        return connection.deviceFingerprintHex;
    }
}