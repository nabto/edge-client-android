package com.nabto.edge.iamutil.mocks

import com.fasterxml.jackson.databind.ser.std.StdArraySerializers.CharArraySerializer
import com.nabto.edge.client.Coap
import com.nabto.edge.client.NabtoCallback
import io.mockk.every
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.junit.Assert.assertEquals
import java.util.Optional

@Serializer(forClass = String::class)
object StringSerializer : KSerializer<String> {
    //override val descriptor: SerialDescriptor = SerialDescriptor("String", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }

    override fun deserialize(decoder: Decoder): String {
        return decoder.decodeString()
    }
}

@Serializable
class UserResponse(val Username : String, val DisplayName : String = "Test User", val Fingerprint : String = "0011223344556677889900112233445566778899001122334455667788990011", val Role : String = "Admin");

fun createGetUserCoapMock(username: String) : Coap {
    val coap : Coap = createCoapMock();

    every { coap.responseStatusCode } returns ( 205 )
    //every { coap.responseContentFormat } returns (Coap.ContentFormat.APPLICATION_CBOR )
    val userResponse : UserResponse = UserResponse(Username = username);
    every { coap.responsePayload } returns ( Cbor.encodeToByteArray(userResponse) )
    return coap
}

@kotlinx.serialization.ExperimentalSerializationApi
fun createPutUserUsernameCoapMock(expectedUsername : String) : Coap {
    val coap : Coap = createCoapMock();
    every { coap.responseStatusCode } returns ( 204 )

    every { coap.setRequestPayload(60, any()) } answers {
        val bytes = secondArg<ByteArray>()
        val newUsername : String = Cbor.decodeFromByteArray<String>(StringSerializer, bytes)
        assertEquals(newUsername, expectedUsername);
    }
    return coap;
}