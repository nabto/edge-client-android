package com.nabto.edge.heatpump

import androidx.fragment.app.FragmentFactory
import com.nabto.edge.heatpump.modules.AppModule
import com.nabto.edge.heatpump.modules.NabtoClientModule
import com.nabto.edge.heatpump.modules.RoomModule
import com.nabto.edge.heatpump.modules.SharedPreferencesModule
import dagger.Component

@Component(modules = [
    HeatPumpFactoryModule::class,
    NabtoClientModule::class,
    SharedPreferencesModule::class,
    AppModule::class,
    RoomModule::class,
    ScanServiceModule::class,
    HeatPumpConnectionFactoryModule::class,
    UnpairedDeviceFactoryModule::class
])
interface HeatPumpComponent {
    fun fragmentFactory(): FragmentFactory

}