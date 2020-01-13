package com.nabto.edge.heatpump.heatpump

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface HeatPumpConnection {
    public enum class ConnectionState {
        CLOSED,
        CONNECTING,
        CONNECTED
    }
    fun getConnectionState() : LiveData<ConnectionState>
    suspend fun connect()
    suspend fun getState() : HeatPumpState
    suspend fun setTarget(target : Double);
    suspend fun setMode(mode : String)
    suspend fun setPower(power : Boolean);
}