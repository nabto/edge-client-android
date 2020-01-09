package com.nabto.edge.heatpump.data.source.heatpump

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nabto.edge.heatpump.data.source.overview.PairedDevice
import com.nabto.edge.heatpump.heatpump.HeatPumpConnection
import com.nabto.edge.heatpump.heatpump.HeatPumpState
import javax.inject.Inject

class FakeHeatPumpConnectionImpl : HeatPumpConnection {


    @Inject constructor(pairedDevice : PairedDevice) {

    }
    var hps = HeatPumpState("HEAT", true, 24.0,23.2)
    override fun getConnectionState(): LiveData<HeatPumpConnection.ConnectionState> {
        return MutableLiveData<HeatPumpConnection.ConnectionState>(HeatPumpConnection.ConnectionState.CONNECTED)
    }

    override suspend fun connect() {
        return;
    }

    override suspend fun getState(): HeatPumpState {
        return hps;
    }

    override suspend fun setPower(power: Boolean) {
        hps.power = power;
    }

    override suspend fun setMode(mode: String) {
        hps.mode = mode;
    }

    override suspend fun setTarget(target: Double) {
        hps.target = target;
    }
}