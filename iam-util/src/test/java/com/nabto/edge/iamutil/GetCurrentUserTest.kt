package com.nabto.edge.iamutil

import com.nabto.edge.client.Connection
import com.nabto.edge.iamutil.mocks.UserResponse
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
class GetCurrentUserTest {
    // hopefully this is througly tested by GetUserTest as they mostly return the same object.
    private var connection : Connection = mockk<Connection>()
    private var iamUtil : IamUtil = IamUtil.create();
    @Before
    fun setup() {
        mockErrorCodes();
    }

    @Test
    fun getCurrentUser()
    {
        val testUserUsername = "testuser"

        every { connection.createCoap("GET", "/iam/me") } returns ( createGetUserCoapMock(
            UserResponse(Username = testUserUsername) ) )
        every { connection.createCoap("GET", "/iam/pairing" ) } returns ( createGetPairingCoapMock() )
        val user = iamUtil.getCurrentUser(connection);
        assertEquals(testUserUsername, user.username);
    }
}
