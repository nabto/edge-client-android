package com.nabto.edge.heatpump

import android.app.Application
import android.content.Context
import android.net.nsd.NsdManager
import androidx.preference.PreferenceManager
import com.nabto.edge.client.NabtoClient
import com.nabto.edge.heatpump.modules.*

class HeatPumpApplication : Application()  {

    lateinit var appComponent : HeatPumpComponent;

    override fun onCreate() {
        super.onCreate()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        val nabtoClient  = NabtoClient.create(this);

        appComponent = DaggerHeatPumpComponent.
                builder().
                scanServiceModule(ScanServiceModule(nabtoClient)).
                nabtoClientModule(NabtoClientModule(NabtoClient.create(this))).
                sharedPreferencesModule(SharedPreferencesModule((sharedPreferences))).
                roomModule(RoomModule(this)).
                build()

    }
}