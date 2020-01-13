package com.nabto.edge.heatpump

import com.nabto.edge.heatpump.data.source.pairing.FakeUnpairedDeviceFactory
import com.nabto.edge.heatpump.pairing.UnpairedDeviceFactory
import dagger.Module
import dagger.Provides
import javax.inject.Inject

@Module
class UnpairedDeviceFactoryModule @Inject constructor () {
    @Provides
    fun proviceUnpairedDeviceFactory() : UnpairedDeviceFactory {
        return FakeUnpairedDeviceFactory()
    }
}