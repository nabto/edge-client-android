package com.nabto.edge.iamutil

import com.nabto.edge.client.Coap.ContentFormat
import com.nabto.edge.client.Connection
import com.nabto.edge.iamutil.mocks.UserResponse
import com.nabto.edge.iamutil.mocks.createCoapMock
import com.nabto.edge.iamutil.mocks.mockCoapGetParing
import com.nabto.edge.iamutil.mocks.mockErrorCodes
import com.nabto.edge.iamutil.mocks.mockPutUserSetting
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.cbor.Cbor
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

@kotlinx.serialization.ExperimentalSerializationApi
class CreateUserTest {
    val connection : Connection = mockk<Connection>();
    val iamUtil = IamUtil.create()
    val username = "username"
    val password = "password"
    val role = "role"

    @Before
    fun setup() {
        mockCoapGetParing(connection)
        mockErrorCodes()
    }
    fun mockPostUsersCoapCall(statusCode: Int)  {
        val coap = createCoapMock()
        every { coap.responseStatusCode } returns statusCode
        val userRequest : UserResponse = UserResponse(Username = username)
        every { coap.setRequestPayload(ContentFormat.APPLICATION_CBOR, any()) } answers {
            val bytes = secondArg<ByteArray>()
            val newUser : UserResponse = Cbor.decodeFromByteArray<UserResponse>(UserResponse.serializer(), bytes)
            assertEquals(newUser.Username, username);
        }
        every { coap.responsePayload } returns ( Cbor.encodeToByteArray(UserResponse.serializer(), userRequest))
        every { connection.createCoap("POST", "/iam/users" ) } returns coap
    }

    fun mockCoapCallWrongData(statusCode: Int) {
        val coap = createCoapMock()
        every { coap.responseStatusCode } returns statusCode
        val stringSerializer: KSerializer<String> = String.serializer()
        every { coap.responsePayload } returns ( Cbor.encodeToByteArray(stringSerializer,"invalid_response_format")  )
        every { connection.createCoap("GET", "/iam/roles" ) } returns coap
    }

    @Test
    fun ok() {
        //val rolesResponse : List<String> = listOf("role1", "role2")
        mockPostUsersCoapCall(201);
        mockPutUserSetting(connection, username, "password", password)
        mockPutUserSetting(connection, username, "role", role)
        iamUtil.createUser(connection, username, password, role)
    }

    @Test
    fun usernameExists() {
        //val rolesResponse : List<String> = listOf("role1", "role2")
        mockPostUsersCoapCall(409);
        mockPutUserSetting(connection, username, "password", password)
        mockPutUserSetting(connection, username, "role", role)
        val exception = assertFailsWith<IamException> {
            iamUtil.createUser(connection, username, password, role)
        }
        assertEquals(exception.getError(), IamError.USERNAME_EXISTS)
    }
}
