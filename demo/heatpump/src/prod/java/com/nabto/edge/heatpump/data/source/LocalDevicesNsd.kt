package com.nabto.edge.heatpump.data.source

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nabto.edge.heatpump.DeviceListItem
import javax.inject.Inject


class LocalDevicesNsd @Inject constructor(private val nsdManager : NsdManager) : LocalDevicesRepository {

    val currentDeviceList = HashMap<String, DeviceListItem>();


    private val devices: MutableLiveData<List<DeviceListItem>> by lazy {
        MutableLiveData<List<DeviceListItem>>().also {
            loadDevices()

        }

    }

    override fun getLocalDevices() : LiveData<List<DeviceListItem>> {
        return devices;

    }

    private fun removeService(host : String, port : Int)
    {
        var key = host + ":" + port;
        currentDeviceList.remove(key);
    }

    private fun addService(productId : String, deviceId : String, host : String, port : Int)
    {
        var key = host+":"+port;
        currentDeviceList[key] = ScanDevice(productId, deviceId, "device");
        devices.postValue(ArrayList(currentDeviceList.values))
    }

    private fun loadDevices() {
        nsdManager.discoverServices("_nabto._udp", NsdManager.PROTOCOL_DNS_SD, object: NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String?) {

            }

            override fun onDiscoveryStopped(serviceType: String?) {

            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
                if (serviceInfo != null) {
                    if (serviceInfo.host != null) {
                        removeService(serviceInfo.host.toString(), serviceInfo.port)
                    }
                }
            }

            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {

            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {

            }
            override fun onServiceFound(service: NsdServiceInfo) {
                if (service.serviceType == "_nabto._udp.") {
                    resolveService(service);

                }
            }

        })
    }

    private fun resolveService(service: NsdServiceInfo) {
        nsdManager.resolveService(service, object: NsdManager.ResolveListener {

            override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {

            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                if (serviceInfo.getServiceType().contains("_nabto._udp")) {


                    val attributes = serviceInfo.getAttributes();

                    val deviceId = attributes.get("deviceId")
                    val productId = attributes.get("productId")
                    if (deviceId != null && productId != null) {
                        addService(String(productId), String(deviceId), serviceInfo.host.toString(), serviceInfo.port)
                    }
                }
            }
        })
    }
}