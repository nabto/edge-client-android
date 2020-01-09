package com.nabto.edge.heatpump.data.source.heatpump

import com.nabto.edge.heatpump.data.source.overview.PairedDevice
import com.nabto.edge.heatpump.heatpump.HeatPumpConnection
import com.nabto.edge.heatpump.heatpump.HeatPumpConnectionFactory

class FakeHeatPumpConnectionFactory : HeatPumpConnectionFactory {
    override fun createHeatPumpConnection(pairedDevice : PairedDevice) : HeatPumpConnection {

        return FakeHeatPumpConnectionImpl(pairedDevice)
    }
}