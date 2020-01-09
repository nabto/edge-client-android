package com.nabto.edge.heatpump.settings

import android.content.SharedPreferences
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nabto.edge.client.NabtoClient
import com.nabto.edge.heatpump.data.source.overview.PairedDevicesDao
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsViewModel @Inject constructor(val pairedDevicesDao : PairedDevicesDao) : ViewModel()  {

    @Inject
    lateinit var client : NabtoClient

    @Inject
    lateinit var sharedPreferences : SharedPreferences

    fun onClickClearListPairedDevices(view : View) {
        viewModelScope.launch {
            pairedDevicesDao.deleteAll();
        }
    }

    fun onClickReCreateKeyPair(view : View) {
        sharedPreferences.edit().putString("PrivateKey", client.createPrivateKey()).commit();
    }
}