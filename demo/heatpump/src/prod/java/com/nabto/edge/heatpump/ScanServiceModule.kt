package com.nabto.edge.heatpump

import com.nabto.edge.client.NabtoClient
import com.nabto.edge.heatpump.data.source.LocalDevicesNabto
import com.nabto.edge.heatpump.data.source.LocalDevicesRepository
import dagger.Module
import dagger.Provides
import javax.inject.Inject


@Module
class ScanServiceModule @Inject constructor (private val nabtoClient : NabtoClient){
    @Provides
    fun provideLocalDevicesRepository() : LocalDevicesRepository {
        return LocalDevicesNabto(nabtoClient);
    }
}