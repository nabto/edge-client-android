package com.nabto.edge.iamutil.mocks

import com.nabto.edge.client.Coap
import com.nabto.edge.client.Connection
import io.mockk.every
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.encodeToByteArray

@Serializable
    class PairingResponse(
        val Modes: Array<String> = arrayOf(),
        val FriendlyName: String = "friendlyname",
        val DeviceId: String = "de-test",
        val ProductId: String = "pr-test",
        val AppVersion: String = "0.0.1",
        val AppName: String = "test",
        val NabtoVersion: String = "0.0.0",
        val Unknown: String = ""
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

fun mockCoapGetPairingError(connection: Connection, statusCode: Int) {
    val coap : Coap = createCoapMock();
    every { coap.execute() } returns ( Unit )
    every { coap.responseStatusCode } returns ( statusCode )

    every { connection.createCoap("GET", "/iam/pairing") } returns coap
}

fun mockCoapGetParing(connection : Connection, pairingResponse : PairingResponse = PairingResponse())
{
    val coap : Coap = createCoapMock();
    every { coap.execute() } returns ( Unit )
    every { coap.responseStatusCode } returns ( 205 )
    //every { coap.responseContentFormat } returns (Coap.ContentFormat.APPLICATION_CBOR )
    val responseData =  Cbor.encodeToByteArray(pairingResponse);
    every { coap.responsePayload } returns ( responseData )

    every { connection.createCoap("GET", "/iam/pairing") } returns coap
}

fun mockCoapGetParingError(connection : Connection, statusCode: Int)
{
    val coap : Coap = createCoapMock();
    every { coap.execute() } returns ( Unit )
    every { coap.responseStatusCode } returns ( statusCode )
    every { connection.createCoap("GET", "/iam/pairing") } returns coap
}
