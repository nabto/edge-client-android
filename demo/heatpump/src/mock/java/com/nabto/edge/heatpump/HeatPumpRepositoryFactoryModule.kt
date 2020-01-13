package com.nabto.edge.heatpump

import com.nabto.edge.heatpump.data.source.heatpump.FakeHeatPumpConnectionFactory
import com.nabto.edge.heatpump.heatpump.HeatPumpConnectionFactory
import dagger.Module
import dagger.Provides
import javax.inject.Inject



@Module
class HeatPumpConnectionFactoryModule @Inject constructor (){
    @Provides
    fun proviceHeatPumpConnectionFactory() : HeatPumpConnectionFactory {
        return FakeHeatPumpConnectionFactory()
    }
}