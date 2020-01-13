package com.nabto.edge.heatpump

import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nabto.edge.heatpump.heatpump.HeatPumpViewModel
import com.nabto.edge.heatpump.overview.OverviewViewModel
import com.nabto.edge.heatpump.pairing.PairDeviceViewModel
import com.nabto.edge.heatpump.scan.ScanViewModel
import com.nabto.edge.heatpump.settings.SettingsViewModel
import javax.inject.Inject
import javax.inject.Provider

class ScanViewModelFactory @Inject constructor(
        private val scanViewModel: Provider<ScanViewModel>,
        private val pairDeviceViewModel: Provider<PairDeviceViewModel>,
        private val overviewViewModel: Provider<OverviewViewModel>,
        private val settingsViewModel: Provider<SettingsViewModel>,
        private val heatPumpViewModel: Provider<HeatPumpViewModel>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass : Class<T>): T {
        return when (modelClass) {
            ScanViewModel::class.java -> scanViewModel.get()
            PairDeviceViewModel::class.java -> pairDeviceViewModel.get()
            OverviewViewModel::class.java -> overviewViewModel.get()
            SettingsViewModel::class.java -> settingsViewModel.get()
            HeatPumpViewModel::class.java -> heatPumpViewModel.get()
            else -> TODO("missing viewModel $modelClass")
        } as T
    }
}