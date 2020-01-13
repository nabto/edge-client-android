package com.nabto.edge.heatpump.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.nabto.edge.heatpump.DeviceListItem
import com.nabto.edge.heatpump.R
import javax.inject.Inject

class ScanFragment
    @Inject constructor(private val factory : ViewModelProvider.Factory): Fragment() {


    private lateinit var listView: ListView


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) : View? {


        //val viewModel by viewModels<ScanViewModel> { factory }
        val viewModel = ViewModelProviders.of(this,  factory)[ScanViewModel::class.java]

        val rootView = inflater.inflate(R.layout.scan_fragment, container, false)


        listView = rootView.findViewById(R.id.scan_list_view) as android.widget.ListView;

        val context = requireContext();
        val adapter = DeviceListAdapter(context, this);
        listView.setAdapter(adapter);

        val deviceObserver = Observer<List<DeviceListItem>> {
            devices ->
                adapter.updateData(devices.toList());
        }

        viewModel.getDiscoveredDevices().observe(this, deviceObserver);

        return rootView
    }
}
