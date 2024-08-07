package com.nabto.edge.iamutil

import com.nabto.edge.client.Connection
import com.nabto.edge.iamutil.mocks.createCoapErrorMock
import com.nabto.edge.iamutil.mocks.createGetPairingCoapMock
import com.nabto.edge.iamutil.mocks.createPutUserUsernameCoapMock
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

@kotlinx.serialization.ExperimentalSerializationApi
class RenameUserTest {
    val connection : Connection = mockk<Connection>();
    val iamUtil = IamUtil.create()

    @Before
    fun setup() {
        every { connection.createCoap("GET", "/iam/pairing" ) } returns ( createGetPairingCoapMock() )
    }


    @Test
    fun renameOk() {
        val testUserUsername = "testuser"
        val newUsername = "new"

        every { connection.createCoap("PUT", "/iam/users/"+testUserUsername+"/username") } returns ( createPutUserUsernameCoapMock(newUsername) )

        iamUtil.renameUser(connection, testUserUsername, newUsername);
    }

    @Test
    fun renameUserDoesNotExists() {
        val testUserUsername = "testuser"
        every { connection.createCoap("PUT", "/iam/users/"+testUserUsername+"/username") } returns ( createCoapErrorMock(404) )
        val exception = assertFailsWith<IamException> {
            iamUtil.renameUser(connection, testUserUsername, "newname")
        }
        assertEquals(exception.getError(), IamError.USER_DOES_NOT_EXIST)
    }

    @Test
    fun invalidUsernameFormat() {
        val testUserUsername = "testuser"
        every { connection.createCoap("PUT", "/iam/users/"+testUserUsername+"/username") } returns ( createCoapErrorMock(400) )
        val exception = assertFailsWith<IamException> {
            iamUtil.renameUser(connection, testUserUsername, "newname")
        }
        assertEquals(exception.getError(), IamError.INVALID_INPUT)
    }

    @Test
    fun missingPermissions() {
        val testUserUsername = "testuser"
        every { connection.createCoap("PUT", "/iam/users/"+testUserUsername+"/username") } returns ( createCoapErrorMock(403) )
        val exception = assertFailsWith<IamException> {
            iamUtil.renameUser(connection, testUserUsername, "newname")
        }
        assertEquals(exception.getError(), IamError.BLOCKED_BY_DEVICE_CONFIGURATION)
    }

    // TODO, this currently fails.
    /*@Test
    fun newUsernameExists() {
        val connection: Connection = mockk<Connection>();
        val iamUtil = IamUtil.create()

        val testUserUsername = "testuser"
        every { connection.createCoap("GET", "/iam/pairing") } returns (createGetPairingCoapMock())
        every {
            connection.createCoap(
                "PUT",
                "/iam/users/" + testUserUsername + "/username"
            )
        } returns (createCoapErrorMock(409))
        val exception = assertFailsWith<IamException> {
            iamUtil.renameUser(connection, testUserUsername, "newname")
        }
        assertEquals(IamError.USERNAME_EXISTS, exception.getError())
    }*/
}
