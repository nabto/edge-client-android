package com.nabto.edge.iamutil

import com.nabto.edge.client.Connection
import com.nabto.edge.iamutil.mocks.createCoapMock
import com.nabto.edge.iamutil.mocks.mockCoapGetParing
import com.nabto.edge.iamutil.mocks.mockErrorCodes
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
class GetAvailableRolesTest {
    val connection : Connection = mockk<Connection>();
    val iamUtil = IamUtil.create()

    @Before
    fun setup() {
        mockCoapGetParing(connection)
        mockErrorCodes()
    }
    fun mockCoapCall(statusCode: Int, roles : List<String> = listOf())  {
        val coap = createCoapMock()
        every { coap.responseStatusCode } returns statusCode
        val stringListSerializer: KSerializer<List<String>> = ListSerializer(String.serializer())
        every { coap.responsePayload } returns ( Cbor.encodeToByteArray(stringListSerializer,roles)  )
        every { connection.createCoap("GET", "/iam/roles" ) } returns coap
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
        val rolesResponse : List<String> = listOf("role1", "role2")
        mockCoapCall(205, rolesResponse);
        val roles = iamUtil.getAvailableRoles(connection)
        assertEquals(rolesResponse.toSet(), roles.toSet());
    }


    @Test
    fun zeroRoles() {
        mockCoapCall(205);
        val roles = iamUtil.getAvailableRoles(connection)
        assertEquals(roles.size, 0)
    }

    @Test
    fun invalidResponseFormat() {
        mockCoapCallWrongData(205);
        val exception = assertFailsWith<IamException> {
            iamUtil.getAvailableRoles(connection)
        }
        assertEquals(exception.getError(), IamError.FAILED)
    }

    @Test
    fun iamBlocked() {
        mockCoapCall(403);
        val exception = assertFailsWith<IamException> {
            iamUtil.getAvailableRoles(connection)
        }
        assertEquals(exception.getError(), IamError.BLOCKED_BY_DEVICE_CONFIGURATION)
    }
}
