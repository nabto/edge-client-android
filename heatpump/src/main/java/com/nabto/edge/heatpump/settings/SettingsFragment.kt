package com.nabto.edge.heatpump.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.nabto.edge.heatpump.R
import com.nabto.edge.heatpump.databinding.PairDeviceFragmentBinding
import com.nabto.edge.heatpump.databinding.SettingsFragmentBinding
import com.nabto.edge.heatpump.pairing.PairDeviceViewModel
import javax.inject.Inject

class SettingsFragment @Inject constructor(private val factory : ViewModelProvider.Factory): Fragment() {

    lateinit private var viewModel : SettingsViewModel
    lateinit private var binding : SettingsFragmentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this, factory)[SettingsViewModel::class.java];
        } ?: throw Exception("Invalid activity")
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) : View? {
        binding = SettingsFragmentBinding.inflate(layoutInflater, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }
}
