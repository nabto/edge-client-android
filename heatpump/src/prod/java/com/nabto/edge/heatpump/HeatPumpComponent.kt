package com.nabto.edge.heatpump

import androidx.fragment.app.FragmentFactory
import com.nabto.edge.heatpump.modules.*
import dagger.Component

@Component(modules = [
    HeatPumpFactoryModule::class,
    ScanServiceModule::class,
    NabtoClientModule::class,
    SharedPreferencesModule::class,
    AppModule::class,
    RoomModule::class,
    FactoryModule::class])
interface HeatPumpComponent {
    fun fragmentFactory(): FragmentFactory
}