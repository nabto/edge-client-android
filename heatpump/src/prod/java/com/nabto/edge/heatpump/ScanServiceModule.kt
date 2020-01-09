package com.nabto.edge.heatpump

import android.net.nsd.NsdManager
import com.nabto.edge.heatpump.data.source.LocalDevicesNsd
import com.nabto.edge.heatpump.data.source.LocalDevicesRepository
import dagger.Module
import dagger.Provides
import javax.inject.Inject

@Module
class ScanServiceModule @Inject constructor (private val nsdManager : NsdManager){
    @Provides
    fun provideScanService() : NsdManager {
        return nsdManager;
    }

    @Provides
    fun provideLocalDevicesRepository() : LocalDevicesRepository {
        return LocalDevicesNsd(nsdManager);
    }
}