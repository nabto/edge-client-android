package com.nabto.edge.heatpump.heatpump

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.nabto.edge.client.Coap
import com.nabto.edge.client.Connection
import com.nabto.edge.client.ConnectionEventsCallback
import com.nabto.edge.client.NabtoClient
import com.nabto.edge.heatpump.data.source.overview.PairedDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

class HeatPumpConnectionImpl @Inject constructor(val pairedDevice: PairedDevice, val nabtoClient : NabtoClient, val sharedPreferences: SharedPreferences) : HeatPumpConnection {

    lateinit var connection : Connection;

    var currentState : HeatPumpCoapState = HeatPumpCoapState();

    var connectionState = MutableLiveData<HeatPumpConnection.ConnectionState>(HeatPumpConnection.ConnectionState.CLOSED);

    var options = JSONObject()

    fun getPrivateKey() : String {
        if (!sharedPreferences.contains("nabto_private_key")) {
            sharedPreferences.edit().putString("nabto_private_key", nabtoClient.createPrivateKey())
        }
        val privKey = sharedPreferences.getString("nabto_private_key", "");
        return privKey!!
    }

    override fun getConnectionState() : LiveData<HeatPumpConnection.ConnectionState>
    {
        return connectionState;
    }

    override public suspend fun connect() {
        connection = nabtoClient.createConnection();
        withContext(Dispatchers.IO) {
            connectionState.postValue(HeatPumpConnection.ConnectionState.CONNECTING)
            listenForConnectionEvents()

            var options = JSONObject()
            options.put("ProductId", pairedDevice.productId)
            options.put("DeviceId", pairedDevice.deviceId);
            //options.put("ServebrUrl", pairedDevice.serverUrl)
            options.put("ServerKey", pairedDevice.serverKey)
            options.put("PrivateKey", getPrivateKey())
            options.put("ServerConnectToken", pairedDevice.sct);

            connection.updateOptions(options.toString())
            connection.connect();
        }
    }

    fun listenForConnectionEvents() {
        connection.addConnectionEventsListener(object: ConnectionEventsCallback() {
            override fun onEvent(event: Int) {
                if (event == ConnectionEventsCallback.CLOSED) {
                    connectionState.postValue(HeatPumpConnection.ConnectionState.CLOSED);
                } else if (event == ConnectionEventsCallback.CONNECTED) {
                    connectionState.postValue(HeatPumpConnection.ConnectionState.CONNECTED)
                }
            }
        });
    }

    override suspend fun getState(): HeatPumpState {
        return withContext(Dispatchers.IO) {
            val coap = connection.createCoap("GET", "/heat-pump")
            coap.execute();
            val responseData = coap.getResponsePayload();
            val responseCode = coap.getResponseStatusCode();

            if (responseCode != 205) {
                throw RuntimeException("invalid response code")
            }


            val f = CBORFactory();
            val mapper = ObjectMapper(f);
            val state = mapper.readValue<HeatPumpCoapState>(responseData, HeatPumpCoapState::class.java)
            return@withContext HeatPumpState(state.mode, state.power, state.target, state.temperature);
        }
    }

    override suspend fun setTarget(target: Double) {
        withContext(Dispatchers.IO) {
            val coap = connection.createCoap("POST", "/heat-pump/target")

            val f = CBORFactory()
            val mapper = ObjectMapper(f);


            val cborData = mapper.writeValueAsBytes(target);
            coap.setRequestPayload(Coap.ContentFormat.APPLICATION_CBOR, cborData)
            coap.execute()
            if (coap.responseStatusCode != 204) {
                throw(Exception("bad response status code" + coap.responseStatusCode))
            }

        }
    }

    override suspend fun setMode(mode: String) {
        withContext(Dispatchers.IO) {
            val coap = connection.createCoap("POST", "/heat-pump/mode")

            val f = CBORFactory()
            val mapper = ObjectMapper(f);


            val cborData = mapper.writeValueAsBytes(mode);
            coap.setRequestPayload(Coap.ContentFormat.APPLICATION_CBOR, cborData)
            coap.execute()
            if (coap.responseStatusCode != 204) {
                throw(Exception("bad response status code" + coap.responseStatusCode))
            }
        }
    }

    override suspend fun setPower(power: Boolean) {
        withContext(Dispatchers.IO) {
            val coap = connection.createCoap("POST", "/heat-pump/power")

            val f = CBORFactory()
            val mapper = ObjectMapper(f);


            val cborData = mapper.writeValueAsBytes(power);
            coap.setRequestPayload(Coap.ContentFormat.APPLICATION_CBOR, cborData)
            coap.execute()
            if (coap.responseStatusCode != 204) {
                throw(Exception("bad response status code" + coap.responseStatusCode))
            }
        }
    }

    public fun isPaired(): Boolean {
        return false;
    }
}