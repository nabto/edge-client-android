package com.nabto.edge.heatpump

import com.nabto.edge.heatpump.heatpump.HeatPumpConnectionFactory
import com.nabto.edge.heatpump.heatpump.HeatPumpConnectionFactoryImpl
import com.nabto.edge.heatpump.pairing.UnpairedDeviceFactory
import com.nabto.edge.heatpump.pairing.UnpairedDeviceFactoryImpl
import dagger.Module
import dagger.Provides

@Module
class FactoryModule {

    @Provides
    fun providesHeatPumpConnectionFactory(heatPumpConnectionFactory: HeatPumpConnectionFactoryImpl): HeatPumpConnectionFactory = heatPumpConnectionFactory

    @Provides
    fun providesUnpairedDeviceFactory(unpairedDeviceFactory: UnpairedDeviceFactoryImpl) : UnpairedDeviceFactory = unpairedDeviceFactory
}