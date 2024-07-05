package com.nabto.edge.iamutil

import com.nabto.edge.client.Coap
import com.nabto.edge.client.Connection
import com.nabto.edge.client.ErrorCode
import com.nabto.edge.client.ErrorCodes
import com.nabto.edge.client.NabtoCallback
import com.nabto.edge.client.NabtoRuntimeException
import com.nabto.edge.iamutil.mocks.createCoapMock
import com.nabto.edge.iamutil.mocks.createGetPairingCoapMock
import com.nabto.edge.iamutil.mocks.mockErrorCodes
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Optional
import kotlin.test.assertFailsWith

@kotlinx.serialization.ExperimentalSerializationApi
class PairPasswordInviteTest {
    val connection : Connection = mockk<Connection>();
    val iamUtil = IamUtil.create()
    val username = "testuser"
    val password = "testpassword"

    @Before
    fun setup() {
        every { connection.createCoap("GET", "/iam/pairing" ) } returns ( createGetPairingCoapMock() )
        mockErrorCodes()
    }

    @Serializable
    class PairingLocalOpenRequestData {
        val Username : String = ""
    }

    fun mockPairingPasswordInviteCall(statusCode : Int) {
        val coap = createCoapMock()
        every { coap.responseStatusCode } returns statusCode

        every { connection.createCoap("POST", "/iam/pairing/password-invite" ) } returns coap
    }

    fun mockPasswordAuthenticate() {
        every { connection.passwordAuthenticate(username, password) } returns Unit
    }

    fun mockPasswordAuthenticateError(errorCodeInt : Int) {
        val runtimeException = mockk<NabtoRuntimeException>();
        //every { runtimeException.cause } returns null
        val errorCode = mockk<ErrorCode>()
        every { errorCode.errorCode } returns errorCodeInt

        every { runtimeException.errorCode } returns errorCode
        every { connection.passwordAuthenticate(username, password) } throws runtimeException
    }

    @Test
    fun ok() {
        mockPasswordAuthenticate()
        mockPairingPasswordInviteCall(201)
        iamUtil.pairPasswordInvite(connection, username, password);
    }

    /*
    @Test
    fun blockedMissingPasswordAuthenticate() {
        mockPasswordAuthenticate()
        mockPairingPasswordInviteCall(401)
        val exception = assertFailsWith<IamException> {
            iamUtil.pairPasswordInvite(connection, username, password);
        }
        assertEquals(exception.getError(), IamError.AUTHENTICATION_ERROR)
    }*/
    @Test
    fun blockedByIam() {
        mockPasswordAuthenticate()
        mockPairingPasswordInviteCall(403)
        val exception = assertFailsWith<IamException> {
            iamUtil.pairPasswordInvite(connection, username, password);
        }
        assertEquals(exception.getError(), IamError.BLOCKED_BY_DEVICE_CONFIGURATION)
    }
    @Test
    fun modeDisabled() {
        mockPasswordAuthenticate()
        mockPairingPasswordInviteCall(404)
        val exception = assertFailsWith<IamException> {
            iamUtil.pairPasswordInvite(connection, username, password);
        }
        assertEquals(exception.getError(), IamError.PAIRING_MODE_DISABLED)
    }

    @Test
    fun passwordAuthenticateTimeout() {
        mockPasswordAuthenticateError(ErrorCodes.TIMEOUT)
        mockPairingPasswordInviteCall(409)
        val exception = assertFailsWith<NabtoRuntimeException> {
            iamUtil.pairPasswordInvite(connection, username, password);
        }
        assertEquals(exception.getErrorCode().getErrorCode(), ErrorCodes.TIMEOUT)
    }

    @Test
    fun passwordAuthenticateNOT_CONNECTED() {
        mockPasswordAuthenticateError(ErrorCodes.NOT_CONNECTED)
        mockPairingPasswordInviteCall(409)

        val exception = assertFailsWith<NabtoRuntimeException> {
            iamUtil.pairPasswordInvite(connection, username, password);
        }
        assertEquals(exception.getErrorCode().getErrorCode(), ErrorCodes.NOT_CONNECTED)
    }

}
