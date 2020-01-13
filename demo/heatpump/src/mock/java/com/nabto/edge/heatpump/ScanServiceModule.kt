package com.nabto.edge.heatpump

import android.net.nsd.NsdManager
import com.nabto.edge.heatpump.data.source.FakeLocalDevices
import com.nabto.edge.heatpump.data.source.LocalDevicesRepository
import dagger.Module
import dagger.Provides
import javax.inject.Inject

@Module
class ScanServiceModule @Inject constructor (){
    @Provides
    fun provideLocalDevicesRepository() : LocalDevicesRepository {
        return FakeLocalDevices()
    }
}