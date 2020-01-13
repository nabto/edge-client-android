package com.nabto.edge.heatpump

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.navigation.fragment.NavHostFragment
import com.nabto.edge.heatpump.heatpump.HeatPumpFragment
import com.nabto.edge.heatpump.overview.OverviewFragment
import com.nabto.edge.heatpump.pairing.*
import com.nabto.edge.heatpump.scan.ScanFragment
import com.nabto.edge.heatpump.settings.SettingsFragment
import javax.inject.Inject
import javax.inject.Provider

class HeatPumpFragmentFactory @Inject constructor(
        private val scanFragmentProvider: Provider<ScanFragment>,
        private val pairDeviceFragmentProvider: Provider<PairDeviceFragment>,


        private val devicePairedFragmentProvider: Provider<DevicePairedFragment>,
        private val remotePairDeviceFragmentProvider: Provider<RemotePairDeviceFragment>,
        private val overviewFragmentProvider: Provider<OverviewFragment>,
        private val settingsFragmentProvider: Provider<SettingsFragment>,
        private val heatpumpFragmentProvider: Provider<HeatPumpFragment>

) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className : String): Fragment {
        return when (className) {
            ScanFragment::class.java.canonicalName -> scanFragmentProvider.get()
            PairDeviceFragment::class.java.canonicalName -> pairDeviceFragmentProvider.get()


            DevicePairedFragment::class.java.canonicalName -> devicePairedFragmentProvider.get()
            RemotePairDeviceFragment::class.java.canonicalName -> remotePairDeviceFragmentProvider.get()
            OverviewFragment::class.java.canonicalName -> overviewFragmentProvider.get()
            NavHostFragment::class.java.canonicalName -> NavHostFragment()
            SettingsFragment::class.java.canonicalName -> settingsFragmentProvider.get()
            HeatPumpFragment::class.java.canonicalName -> heatpumpFragmentProvider.get()
            else -> TODO("missing fragment $className")
        }
    }
}