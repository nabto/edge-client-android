package com.nabto.edge.heatpump.data.source

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nabto.edge.client.MdnsResult
import com.nabto.edge.client.MdnsResultListener
import com.nabto.edge.client.NabtoClient
import com.nabto.edge.heatpump.DeviceListItem
import javax.inject.Inject


class LocalDevicesNabto @Inject constructor(private val nabtoClient : NabtoClient) : LocalDevicesRepository {

    val currentDeviceList = HashMap<String, DeviceListItem>();

    private val devices: MutableLiveData<List<DeviceListItem>> by lazy {
        MutableLiveData<List<DeviceListItem>>().also {
            loadDevices()
        }
    }

    override fun getLocalDevices() : LiveData<List<DeviceListItem>> {
        return devices;
    }

    private fun removeService(serviceInstanceName : String)
    {
        currentDeviceList.remove(serviceInstanceName);
    }

    private fun addService(productId : String, deviceId : String, key : String)
    {
        currentDeviceList[key] = ScanDevice(productId, deviceId, "device");
        devices.postValue(ArrayList(currentDeviceList.values))
    }

    private fun loadDevices() {
        nabtoClient.addMdnsResultListener(object: MdnsResultListener {
            override fun onChange(result: MdnsResult?) {
                val action = result?.action;
                if (action == MdnsResult.Action.ADD || action == MdnsResult.Action.UPDATE) {
                    val deviceId = result?.deviceId;
                    val productId = result?.productId;
                    val serviceInstanceName = result?.serviceInstanceName;
                    addService(productId, deviceId, serviceInstanceName);
                } else if (result?.getAction() == MdnsResult.Action.REMOVE) {
                    val serviceInstanceName = result?.serviceInstanceName;
                    removeService(serviceInstanceName);
                }
            }
        });
    }
}