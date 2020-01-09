package com.nabto.edge.heatpump.modules

import com.nabto.edge.client.NabtoClient
import dagger.Module
import dagger.Provides
import javax.inject.Inject

@Module
class NabtoClientModule @Inject constructor (private val nabtoClient : NabtoClient) {
    @Provides
    fun provideNabtoClient() : NabtoClient {
        return nabtoClient
    }
}