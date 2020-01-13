package com.nabto.edge.heatpump.data.source

import com.nabto.edge.heatpump.DeviceListItem

class FakeDevice(override val productId : String,
                 override val deviceId : String,
                 override val friendlyName : String ) : DeviceListItem {

}