package com.nabto.edge.iamutil

import com.nabto.edge.client.Coap
import com.nabto.edge.client.Connection
import com.nabto.edge.iamutil.mocks.createCoapMock
import com.nabto.edge.iamutil.mocks.createCoapMock404
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.encodeToByteArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import kotlin.test.assertFailsWith

@kotlinx.serialization.ExperimentalSerializationApi
class GetUserTest {

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

    @Serializable
    class UserResponse(val Username : String, val DisplayName : String = "Test User", val Fingerprint : String = "0011223344556677889900112233445566778899001122334455667788990011", val Role : String = "Admin");

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

    fun createGetUserCoapMoxk(username: String) : Coap {
        val coap : Coap = createCoapMock();
        every { coap.execute() } returns ( Unit )
        every { coap.responseStatusCode } returns ( 205 )
        //every { coap.responseContentFormat } returns (Coap.ContentFormat.APPLICATION_CBOR )
        val userResponse : UserResponse = UserResponse(Username = username);
        every { coap.responsePayload } returns ( Cbor.encodeToByteArray(userResponse) )
        return coap
    }

    @Test
    fun getUser()
    {

        val connection : Connection = mockk<Connection>();
        val iamUtil = IamUtil.create()

        val testUserUsername = "testuser"

        every { connection.createCoap("GET", "/iam/users/"+testUserUsername) } returns ( createGetUserCoapMoxk(testUserUsername) )
        every { connection.createCoap("GET", "/iam/pairing" ) } returns ( createGetPairingCoapMock() )
        val user = iamUtil.getUser(connection, testUserUsername);
        assertEquals(testUserUsername, user.username);
    }

    @Test
    fun getUserIamDisabled() {
        val connection : Connection = mockk<Connection>();
        val iamUtil = IamUtil.create()
        val testUserUsername = "testuser"

        every { connection.createCoap("GET", "/iam/users/"+testUserUsername) } returns ( createCoapMock404() )
        every { connection.createCoap("GET", "/iam/pairing" ) } returns ( createCoapMock404() )
        val exception = assertFailsWith<IamException> {
            iamUtil.getUser(connection, testUserUsername)
        }
        assertEquals(exception.getError(), IamError.IAM_NOT_SUPPORTED)
    }

    @Test
    fun getUserNoSuchUser() {
        val connection : Connection = mockk<Connection>();
        val iamUtil = IamUtil.create()
        val testUserUsername = "testuser"

        every { connection.createCoap("GET", "/iam/users/"+testUserUsername) } returns ( createCoapMock404() )
        every { connection.createCoap("GET", "/iam/pairing" ) } returns ( createGetPairingCoapMock() )
        val exception = assertFailsWith<IamException> {
            iamUtil.getUser(connection, testUserUsername)
        }
        assertEquals(exception.getError(), IamError.USER_DOES_NOT_EXIST)
    }
}
