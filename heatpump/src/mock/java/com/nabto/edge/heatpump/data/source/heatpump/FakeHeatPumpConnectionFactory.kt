package com.nabto.edge.heatpump.data.source.heatpump

import com.nabto.edge.heatpump.HeatPumpRepositoryFactory
import com.nabto.edge.heatpump.PairedDevice
import com.nabto.edge.heatpump.heatpump.HeatPumpConnection
import com.nabto.edge.heatpump.heatpump.HeatPumpConnectionFactory
import com.nabto.edge.heatpump.heatpump.HeatPumpRepository

class FakeHeatPumpConnectionFactory : HeatPumpConnectionFactory {
    override fun createHeatPumpConnection(pairedDevice : PairedDevice) : HeatPumpConnection {
        return FakeHeatPumpConnectionImpl(pairedDevice)
    }
}