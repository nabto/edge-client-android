package com.nabto.edge.heatpump.pairing

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.nabto.edge.heatpump.R
import com.nabto.edge.heatpump.databinding.PairDeviceFragmentBinding
import com.nabto.edge.heatpump.heatpump.HeatPumpFragmentArgs
import javax.inject.Inject

class PairDeviceFragment @Inject constructor(private val factory : ViewModelProvider.Factory): Fragment() {

    lateinit var viewModel : PairDeviceViewModel
    lateinit var binding : PairDeviceFragmentBinding

    val args : PairDeviceFragmentArgs by navArgs<PairDeviceFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this, factory)[PairDeviceViewModel::class.java];
        } ?: throw Exception("Invalid activity")
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) : View? {



        binding = PairDeviceFragmentBinding.inflate(layoutInflater, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        if (args.productId != null && args.deviceId != null) {
            viewModel.localConnect(args.productId, args.deviceId);
        }

        // navigate to the heatpump view when the device has been paired
        viewModel.state.observe(this) { state ->
            if (state == PairDeviceViewModel.State.DONE) {
                val navController = Navigation.findNavController(binding.root)

                val pairedDest = PairDeviceFragmentDirections.heatpumpPaired(viewModel.productId.value!!, viewModel.deviceId.value!!)

                navController.navigate(pairedDest)
            }

        }

        return binding.root
    }
}
