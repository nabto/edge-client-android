package com.nabto.edge.heatpump.pairing

interface PairDeviceDao {
    suspend fun connect();
    suspend fun pairing();
}