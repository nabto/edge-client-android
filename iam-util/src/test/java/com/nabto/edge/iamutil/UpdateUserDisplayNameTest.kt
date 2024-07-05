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
class UpdateUserDisplayNameTest {
    val connection : Connection = mockk<Connection>();
    val iamUtil = IamUtil.create()
    val username = "username"

    @Before
    fun setup() {
        mockCoapGetParing(connection)
        mockErrorCodes()
    }

    @Test
    fun ok() {
        mockPutUserSetting(connection, username, "display-name", "value")
        iamUtil.updateUserDisplayName(connection, username, "value")
    }
}
