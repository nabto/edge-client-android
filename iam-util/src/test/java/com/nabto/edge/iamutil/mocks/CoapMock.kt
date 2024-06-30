package com.nabto.edge.iamutil.mocks

import com.nabto.edge.client.Coap
import com.nabto.edge.iamutil.GetUserTest.PairingResponse
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.encodeToByteArray

fun createCoapMock() : Coap {
    val coap : Coap = mockk<Coap>();
    every { coap.close() } returns ( Unit )
    return coap;
}

fun createCoapMock404() : Coap {
    val coap : Coap = createCoapMock();
    every { coap.execute() } returns ( Unit )
    every { coap.responseStatusCode } returns ( 404 )
    //every { coap.responseContentFormat } returns (Coap.ContentFormat.APPLICATION_CBOR )
    //val pairingResponse : PairingResponse = PairingResponse();
    //val responseData =  Cbor.encodeToByteArray(pairingResponse);
    //every { coap.responsePayload } returns ( responseData )
    return coap
}

class CoapMock {

}
