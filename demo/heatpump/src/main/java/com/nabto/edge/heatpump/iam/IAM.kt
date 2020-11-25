package com.nabto.edge.heatpump.iam

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.nabto.edge.client.Connection
import com.nabto.edge.heatpump.pairing.UnpairedDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IAM {
    lateinit private var connection : Connection;

    constructor(connection : Connection) {
        this.connection = connection;
    }
    public class IAMUser {
        @JsonProperty("Username", required = true) var username : String = ""
        @JsonProperty("Role", required = false) var role : String = ""
        @JsonProperty("Fingerprint", required = false) var fingerprint : String = ""
        @JsonProperty("DisplayName", required = false) var displayName : String = ""
        @JsonProperty("Sct", required = false) var sct : String = ""
    }

    public class IAMPairing {
        @JsonProperty("Modes", required = false) var modes : List<String> = listOf()
        @JsonProperty("NabtoVersion", required = false) var nabtoVersion : String = ""
        @JsonProperty("AppVersion", required = false) var appVersion : String = ""
        @JsonProperty("AppName", required = false) var appName : String = ""
        @JsonProperty("ProductId", required = false) var productId : String = ""
        @JsonProperty("DeviceId", required = false) var deviceId : String = ""
    }

    public suspend fun getMe() : IAMUser {
        return withContext(Dispatchers.IO) {
            val csCoap = connection.createCoap("GET", "/iam/me")
            csCoap.execute()
            if (csCoap.responseStatusCode != 205) {
                throw(Exception("Maybe Not paired " + csCoap.responseStatusCode));
            }
            val f = CBORFactory();
            val mapper = ObjectMapper(f);
            val user = mapper.readValue<IAMUser>(csCoap.responsePayload, IAMUser::class.java)
            return@withContext user
        }
    }

    suspend fun getPairing() : IAMPairing {
        return withContext(Dispatchers.IO) {
            val csCoap = connection.createCoap("GET", "/iam/pairing")
            csCoap.execute()
            if (csCoap.responseStatusCode != 205) {
                throw(Exception("Could not get pairing" + csCoap.responseStatusCode));
            }
            val f = CBORFactory();
            val mapper = ObjectMapper(f);
            val pairing = mapper.readValue<IAMPairing>(csCoap.responsePayload, IAMPairing::class.java)
            return@withContext pairing;
        }
    }

    suspend fun localInitialPairing() {
        return withContext(Dispatchers.IO) {
            val csCoap = connection.createCoap("POST", "/iam/pairing/local-initial")
            csCoap.execute()
            if (csCoap.responseStatusCode != 201) {
                throw(Exception("Pairing failed" + csCoap.responseStatusCode));
            }
            return@withContext;
        }
    }
}