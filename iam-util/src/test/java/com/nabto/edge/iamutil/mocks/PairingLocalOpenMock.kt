package com.nabto.edge.iamutil.mocks

import com.nabto.edge.client.Coap
import io.mockk.every
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import org.junit.Assert.assertEquals

@Serializable
class PairingLocalOpenRequestData {
    val Username : String = ""
}

fun pairingLocalOpenCoapMock(username: String, statusCode: Int) : Coap {
    val coap = createCoapMock()
    every { coap.setRequestPayload(60, any()) } answers {
        val bytes = secondArg<ByteArray>()
        val request : PairingLocalOpenRequestData = Cbor{ignoreUnknownKeys = true}.decodeFromByteArray<PairingLocalOpenRequestData>(PairingLocalOpenRequestData.serializer(), bytes)
        assertEquals(request.Username, username);
    }
    every { coap.responseStatusCode } returns statusCode
    return coap
}

fun pairingLocalInitialCoapMock(statusCode: Int) : Coap {
    val coap = createCoapMock()
    every { coap.responseStatusCode } returns statusCode
    return coap
}
