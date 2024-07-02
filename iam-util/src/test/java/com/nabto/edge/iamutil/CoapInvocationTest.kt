package com.nabto.edge.iamutil

import com.nabto.edge.client.Connection
import com.nabto.edge.client.ErrorCodes
import com.nabto.edge.client.NabtoRuntimeException
import com.nabto.edge.iamutil.mocks.createCoapExecuteErrorMock
import com.nabto.edge.iamutil.mocks.createGetPairingCoapMock
import com.nabto.edge.iamutil.mocks.createGetUserCoapMock
import com.nabto.edge.iamutil.mocks.mockErrorCodes
import com.nabto.edge.iamutil.mocks.mockErrorCodes2
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Optional
import java.util.concurrent.CountDownLatch
import kotlin.test.assertFailsWith

/**
 * Test generic coap invocation errors such as when a coap execute fails for various reasons.
 * For these tests we just use a common iam function such getUser
 */
class CoapInvocationTest {
    private var connection : Connection = mockk<Connection>()
    private var iamUtil : IamUtil = IamUtil.create()
    @Before
    fun setup() {
        mockErrorCodes();
    }

    @Test
    fun coapExecuteTimeout() {
        val testUserUsername = "testuser"
        every { connection.createCoap("GET", "/iam/users/"+testUserUsername) } returns ( createCoapExecuteErrorMock(ErrorCodes.TIMEOUT) )
        every { connection.createCoap("GET", "/iam/pairing" ) } returns ( createGetPairingCoapMock() )

        val exception = assertFailsWith<NabtoRuntimeException> {
            iamUtil.getUser(connection, testUserUsername)
        }
        assertEquals(ErrorCodes.TIMEOUT, exception.getErrorCode().getErrorCode())
    }

    @Test
    fun coapExecuteStopped() {
        val testUserUsername = "testuser"
        every { connection.createCoap("GET", "/iam/users/"+testUserUsername) } returns ( createCoapExecuteErrorMock(ErrorCodes.STOPPED) )
        every { connection.createCoap("GET", "/iam/pairing" ) } returns ( createGetPairingCoapMock() )

        val exception = assertFailsWith<NabtoRuntimeException> {
            iamUtil.getUser(connection, testUserUsername)
        }
        assertEquals(ErrorCodes.STOPPED, exception.getErrorCode().getErrorCode())
    }

    @Test
    fun coapExecuteNotConnected() {
        val testUserUsername = "testuser"
        every { connection.createCoap("GET", "/iam/users/"+testUserUsername) } returns ( createCoapExecuteErrorMock(ErrorCodes.NOT_CONNECTED) )
        every { connection.createCoap("GET", "/iam/pairing" ) } returns ( createGetPairingCoapMock() )

        val exception = assertFailsWith<NabtoRuntimeException> {
            iamUtil.getUser(connection, testUserUsername)
        }
        assertEquals(ErrorCodes.NOT_CONNECTED, exception.getErrorCode().getErrorCode())
    }

    @Test
    fun coapExecuteTimeoutCallback() {
        val testUserUsername = "testuser"
        every { connection.createCoap("GET", "/iam/users/"+testUserUsername) } returns ( createCoapExecuteErrorMock(ErrorCodes.NOT_CONNECTED) )
        every { connection.createCoap("GET", "/iam/pairing" ) } returns ( createGetPairingCoapMock() )

        var called = false
        val latch : CountDownLatch = CountDownLatch(1)
        iamUtil.getUserCallback(connection, testUserUsername, { iamError: IamError, _ : Optional<IamUser> ->
            run {
                called = true
                assertEquals(iamError, IamError.FAILED)

                latch.countDown()
            }
        })
        latch.await()
        assertTrue(called);
    }
}
