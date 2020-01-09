package com.nabto.edge.heatpump

import android.app.Application
import android.content.Context
import android.net.nsd.NsdManager
import androidx.preference.PreferenceManager
import com.nabto.edge.client.NabtoClient

class HeatPumpApplication : Application()  {

    lateinit var appComponent : HeatPumpComponent
    override fun onCreate() {
        super.onCreate()
        val nsdManager = getSystemService(Context.NSD_SERVICE) as NsdManager;

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


        appComponent = DaggerHeatPumpComponent.
                builder().
                nabtoClientModule(NabtoClientModule(NabtoClient.create(this))).
                sharedPreferencesModule(SharedPreferencesModule((sharedPreferences))).
                appModule(AppModule(this)).
                roomModule(RoomModule(this)).
                build()


    }
}