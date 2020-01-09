package com.nabto.edge.heatpump.pairing

interface PairDeviceDaoFactory {
    fun createPairDeviceDao(productId : String, deviceId : String) : PairDeviceDao
}