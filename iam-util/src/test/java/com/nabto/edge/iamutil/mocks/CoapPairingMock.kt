package com.nabto.edge.iamutil.mocks

import com.nabto.edge.client.Coap
import io.mockk.every
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.encodeToByteArray

@Serializable
    class PairingResponse(
        val Modes: Array<String> = arrayOf("LocalInitial"),
        val FriendlyName: String = "friendlyname",
        val DeviceId: String = "de-test",
        val ProductId: String = "pr-test",
        val AppVersion: String = "0.0.1",
        val AppName: String = "test",
        val NabtoVersion: String = "0.0.0"
    )



    fun createGetPairingCoapMock() : Coap {
        val coap : Coap = createCoapMock();
        every { coap.execute() } returns ( Unit )
        every { coap.responseStatusCode } returns ( 205 )
        //every { coap.responseContentFormat } returns (Coap.ContentFormat.APPLICATION_CBOR )
        val pairingResponse : PairingResponse = PairingResponse();
        val responseData =  Cbor.encodeToByteArray(pairingResponse);
        every { coap.responsePayload } returns ( responseData )
        return coap
    }
