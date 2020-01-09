package com.nabto.edge.heatpump.data.source.heatpump

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nabto.edge.heatpump.PairedDevice
import com.nabto.edge.heatpump.heatpump.HeatPumpConnection
import com.nabto.edge.heatpump.heatpump.HeatPumpRepository
import com.nabto.edge.heatpump.heatpump.HeatPumpState
import javax.inject.Inject

class FakeHeatPumpConnectionImpl : HeatPumpConnection {


    @Inject constructor(pairedDevice : PairedDevice) {

    }
    var hps = HeatPumpState("HEAT", true, 24.0,23.2)

    override fun connect() {
        return;
    }

    override fun getState(): HeatPumpState {
        return hps;
    }

    override fun setPower(power: Boolean) {
        hps.power = power;
    }

    override fun setMode(mode: String) {
        hps.mode = mode;
    }

    override fun setTarget(target: Double) {
        hps.target = target;
    }
}