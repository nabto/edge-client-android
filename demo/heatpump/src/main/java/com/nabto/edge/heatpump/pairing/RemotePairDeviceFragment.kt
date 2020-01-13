package com.nabto.edge.heatpump.pairing

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.nabto.edge.heatpump.R
import com.nabto.edge.heatpump.databinding.RemotePairDeviceFragmentBinding
import javax.inject.Inject

class RemotePairDeviceFragment
@Inject constructor (val factory : ViewModelProvider.Factory): Fragment() {

    lateinit var viewModel : PairDeviceViewModel

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

        if (arguments != null) {
            val queryString = arguments?.getString("args");
            val uri = Uri.parse("http://foo.bar.baz/?" + queryString);
            val productId = uri.getQueryParameter("product_id")
            val deviceId = uri.getQueryParameter("device_id");
            val serverKey = uri.getQueryParameter("server_key")
            val serverUrl = uri.getQueryParameter("server_url")

            viewModel.productId.postValue(productId)
            viewModel.deviceId.postValue(deviceId)
            viewModel.serverKey.postValue(serverKey)
            viewModel.serverUrl.postValue(serverUrl)
        }

        val binding = RemotePairDeviceFragmentBinding.inflate(layoutInflater, container, false)

        binding.viewModel = viewModel

        binding.setLifecycleOwner { lifecycle }

        return binding.root;
    }

}