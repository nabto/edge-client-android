package com.nabto.edge.heatpump.heatpump

import com.nabto.edge.heatpump.data.source.overview.PairedDevice
import javax.inject.Inject

interface HeatPumpConnectionFactory {
    fun createHeatPumpConnection(device : PairedDevice) : HeatPumpConnection
}