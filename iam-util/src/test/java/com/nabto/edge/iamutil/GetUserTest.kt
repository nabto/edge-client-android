package com.nabto.edge.iamutil

import com.nabto.edge.client.Connection
import com.nabto.edge.iamutil.mocks.createCoapErrorMock
import com.nabto.edge.iamutil.mocks.createGetPairingCoapMock
import com.nabto.edge.iamutil.mocks.createGetUserCoapMock
import com.nabto.edge.iamutil.mocks.mockErrorCodes
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

@kotlinx.serialization.ExperimentalSerializationApi
class GetUserTest {
    private var connection : Connection = mockk<Connection>()
    private var iamUtil : IamUtil = IamUtil.create();
    @Before
    fun setup() {
        mockErrorCodes();
    }

    @Test
    fun getUser()
    {
        val testUserUsername = "testuser"

        every { connection.createCoap("GET", "/iam/users/"+testUserUsername) } returns ( createGetUserCoapMock(testUserUsername) )
        every { connection.createCoap("GET", "/iam/pairing" ) } returns ( createGetPairingCoapMock() )
        val user = iamUtil.getUser(connection, testUserUsername);
        assertEquals(testUserUsername, user.username);
    }

    @Test
    fun getUserCallback()
    {
        val testUserUsername = "testuser"

        every { connection.createCoap("GET", "/iam/users/"+testUserUsername) } returns ( createGetUserCoapMock(testUserUsername) )
        every { connection.createCoap("GET", "/iam/pairing" ) } returns ( createGetPairingCoapMock() )
        val latch : CountDownLatch = CountDownLatch(1)
        iamUtil.getUserCallback(connection, testUserUsername, { iamError: IamError, _ : Optional<IamUser> ->
            run {
                assertEquals(IamError.NONE, iamError)
                latch.countDown();
            }
        })
        latch.await()
    }

    @Test
    fun getUserIamDisabled() {
        val testUserUsername = "testuser"

        every { connection.createCoap("GET", "/iam/users/"+testUserUsername) } returns ( createCoapErrorMock(404) )
        every { connection.createCoap("GET", "/iam/pairing" ) } returns ( createCoapErrorMock(404) )
        val exception = assertFailsWith<IamException> {
            iamUtil.getUser(connection, testUserUsername)
        }
        assertEquals(exception.getError(), IamError.IAM_NOT_SUPPORTED)
    }

    @Test
    fun getUserNoSuchUser() {
        val testUserUsername = "testuser"

        every { connection.createCoap("GET", "/iam/users/"+testUserUsername) } returns ( createCoapErrorMock(404) )
        every { connection.createCoap("GET", "/iam/pairing" ) } returns ( createGetPairingCoapMock() )
        val exception = assertFailsWith<IamException> {
            iamUtil.getUser(connection, testUserUsername)
        }
        assertEquals(exception.getError(), IamError.USER_DOES_NOT_EXIST)
    }

    @Test
    fun getUserDenied() {
        val testUserUsername = "testuser"

        every { connection.createCoap("GET", "/iam/users/"+testUserUsername) } returns ( createCoapErrorMock(403) )
        every { connection.createCoap("GET", "/iam/pairing" ) } returns ( createGetPairingCoapMock() )
        val exception = assertFailsWith<IamException> {
            iamUtil.getUser(connection, testUserUsername)
        }
        assertEquals(exception.getError(), IamError.BLOCKED_BY_DEVICE_CONFIGURATION)
    }
}
