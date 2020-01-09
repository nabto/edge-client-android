package com.nabto.edge.heatpump.data.source

import com.nabto.edge.heatpump.DeviceListItem

data class ScanDevice(override val productId : String, override val deviceId : String, override val friendlyName : String) : DeviceListItem
