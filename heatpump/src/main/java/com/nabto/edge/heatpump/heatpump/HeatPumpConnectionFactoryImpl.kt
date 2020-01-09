package com.nabto.edge.heatpump.heatpump

import android.content.SharedPreferences
import com.nabto.edge.client.NabtoClient
import com.nabto.edge.heatpump.data.source.overview.PairedDevice
import com.nabto.edge.heatpump.scan.ScanFragment
import javax.inject.Inject
import javax.inject.Provider

class HeatPumpConnectionFactoryImpl
@Inject constructor(val nabtoClient : NabtoClient, val sharedPreferences: SharedPreferences) : HeatPumpConnectionFactory {
    override fun createHeatPumpConnection(device: PairedDevice): HeatPumpConnection {
        return HeatPumpConnectionImpl(device, nabtoClient, sharedPreferences);
    }
}