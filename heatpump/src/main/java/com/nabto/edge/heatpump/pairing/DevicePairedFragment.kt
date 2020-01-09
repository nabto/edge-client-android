package com.nabto.edge.heatpump.pairing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.nabto.edge.heatpump.R
import javax.inject.Inject

class DevicePairedFragment @Inject constructor(): Fragment() {

    @Inject
    lateinit var factory : ViewModelProvider.Factory

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) : View? {
        val viewModel = ViewModelProviders.of(this, factory).get(PairDeviceViewModel::class.java)
        val rootView = inflater.inflate(R.layout.device_paired_fragment, container, false)
        return rootView;
    }
}
