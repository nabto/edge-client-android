package com.nabto.edge.heatpump.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.nabto.edge.heatpump.DeviceListItem
import com.nabto.edge.heatpump.R
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.nabto.edge.heatpump.databinding.OverviewFragmentBinding
import com.nabto.edge.heatpump.databinding.PairDeviceFragmentBinding
import com.nabto.edge.heatpump.scan.DeviceListAdapter
import javax.inject.Inject

class OverviewFragment
    @Inject constructor(val factory : ViewModelProvider.Factory): Fragment()
{
    lateinit private var viewModel : OverviewViewModel

    private lateinit var listView: ListView
    lateinit var binding : OverviewFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this, factory)[OverviewViewModel::class.java];
        } ?: throw Exception("Invalid activity")
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) : View? {

        binding = OverviewFragmentBinding.inflate(layoutInflater, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val rootView  = binding.root;

        listView = rootView.findViewById(R.id.list_view) as android.widget.ListView;

        val context = requireContext();
        val adapter = OverviewDeviceListAdapter(context, this);
        listView.setAdapter(adapter);

        val deviceObserver = Observer<List<DeviceListItem>> {
            devices ->
                adapter.updateData(devices);
        }
        viewModel.allPairedDevices.observe(this, deviceObserver);

        return rootView
    }
}
