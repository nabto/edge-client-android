package com.nabto.edge.heatpump.overview;

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation
import com.nabto.edge.heatpump.*
import com.nabto.edge.heatpump.data.source.overview.PairedDevice
import com.nabto.edge.heatpump.data.source.overview.PairedDevicesDao
import com.nabto.edge.heatpump.data.source.overview.PairedDevicesRepository
import javax.inject.Inject


class OverviewViewModel
@Inject constructor(val pairedDevicesDao : PairedDevicesDao) : ViewModel() {

    private val repository : PairedDevicesRepository

    val allPairedDevices : LiveData<List<PairedDevice>>

    init {
        repository = PairedDevicesRepository(pairedDevicesDao)
        allPairedDevices = repository.allPairedDevices

    }

    fun onClickAddNew(view : View) {
        val navController = Navigation.findNavController(view)
        navController.navigate(R.id.scan_dest)
    }
}
