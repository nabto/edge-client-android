package com.nabto.edge.heatpump.data.source.overview

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.nabto.edge.heatpump.DeviceListItem


@Entity(tableName = "paired_devices", primaryKeys = arrayOf("product_id", "device_id"))
data class PairedDevice (
        @ColumnInfo(name = "product_id") override val productId: String,
        @ColumnInfo(name = "device_id") override val deviceId: String,
        @ColumnInfo(name = "server_url") val serverUrl: String,
        @ColumnInfo(name = "server_key") val serverKey: String,
        @ColumnInfo(name = "device_fingerprint") val deviceFingerprint: String,
        @ColumnInfo(name = "friendly_name") override val friendlyName: String
) : DeviceListItem