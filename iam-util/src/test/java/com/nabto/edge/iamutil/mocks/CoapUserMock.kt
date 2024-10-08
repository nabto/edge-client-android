package com.nabto.edge.iamutil.mocks

import com.fasterxml.jackson.databind.ser.std.StdArraySerializers.CharArraySerializer
import com.nabto.edge.client.Coap
import com.nabto.edge.client.Connection
import com.nabto.edge.client.NabtoCallback
import io.mockk.every
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.junit.Assert.assertEquals
import java.util.Optional

@Serializable
class UserResponse(
    val Username : String,
    val DisplayName : String? = "Test User",
    val Fingerprint : String? = "0011223344556677889900112233445566778899001122334455667788990011",
    val Sct : String? = "",
    val Role : String? = "Admin"
);

@kotlinx.serialization.ExperimentalSerializationApi
fun createGetUserCoapMock(userResponse : UserResponse) : Coap {
    val coap : Coap = createCoapMock();

    every { coap.responseStatusCode } returns ( 205 )
    // every { coap.responseContentFormat } returns (Coap.ContentFormat.APPLICATION_CBOR )
    every { coap.responsePayload } returns ( Cbor.encodeToByteArray(userResponse) )
    return coap
}

@kotlinx.serialization.ExperimentalSerializationApi
fun mockCoapGetMe(connection : Connection, username: String) {
    every { connection.createCoap("GET", "/iam/me") } returns createGetUserCoapMock(UserResponse(Username = username) );
}

fun mockCoapGetMeError(connection : Connection, statusCode : Int) {
    every { connection.createCoap("GET", "/iam/me") } returns createCoapErrorMock(statusCode);
}

@kotlinx.serialization.ExperimentalSerializationApi
fun createPutUserUsernameCoapMock(expectedUsername : String) : Coap {
    val coap : Coap = createCoapMock();
    every { coap.responseStatusCode } returns ( 204 )

    every { coap.setRequestPayload(60, any()) } answers {
        val bytes = secondArg<ByteArray>()
        val stringSerializer: KSerializer<String> = String.serializer()
        val newUsername : String = Cbor.decodeFromByteArray<String>(stringSerializer, bytes)
        assertEquals(newUsername, expectedUsername);
    }
    return coap;
}

@kotlinx.serialization.ExperimentalSerializationApi
fun mockPutUserSetting(connection: Connection, username : String, setting : String, expected : String)  {
    val coap : Coap = createCoapMock();
    every { coap.responseStatusCode } returns ( 204 )

    every { coap.setRequestPayload(60, any()) } answers {
        val bytes = secondArg<ByteArray>()
        val stringSerializer: KSerializer<String> = String.serializer()
        val item : String = Cbor.decodeFromByteArray<String>(stringSerializer, bytes)
        assertEquals(item, expected);
    }
    every { connection.createCoap("PUT", "/iam/users/" + username + "/" + setting) } returns coap
}


@kotlinx.serialization.ExperimentalSerializationApi
fun mockDeleteUser(connection: Connection, username : String)  {
    val coap : Coap = createCoapMock();
    every { coap.responseStatusCode } returns ( 202 )

    every { connection.createCoap("DELETE", "/iam/users/" + username) } returns coap
}
