package com.nabto.edge.iamutil

import com.nabto.edge.client.Coap
import com.nabto.edge.client.Connection
import com.nabto.edge.client.ErrorCode
import com.nabto.edge.client.ErrorCodes
import com.nabto.edge.client.NabtoCallback
import com.nabto.edge.client.NabtoRuntimeException
import com.nabto.edge.iamutil.mocks.PairingResponse
import com.nabto.edge.iamutil.mocks.createCoapMock
import com.nabto.edge.iamutil.mocks.createGetPairingCoapMock
import com.nabto.edge.iamutil.mocks.mockCoapGetMe
import com.nabto.edge.iamutil.mocks.mockCoapGetMeError
import com.nabto.edge.iamutil.mocks.mockCoapGetPairingError
import com.nabto.edge.iamutil.mocks.mockCoapGetParing
import com.nabto.edge.iamutil.mocks.mockCoapGetParingError
import com.nabto.edge.iamutil.mocks.mockErrorCodes
import io.mockk.core.ValueClassSupport.maybeUnboxValueForMethodReturn
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Optional
import kotlin.test.assertFailsWith

@kotlinx.serialization.ExperimentalSerializationApi
class IsCurrentUserPairedTest {
    val connection : Connection = mockk<Connection>();
    val iamUtil = IamUtil.create()
    var username = "testuser";

    @Before
    fun setup() {
        mockCoapGetParing(connection)
        mockErrorCodes()
    }

    @Test
    fun paired() {
        mockCoapGetParing(connection)
        mockCoapGetMe(connection, username);
        val isPaired = iamUtil.isCurrentUserPaired(connection)
        assertTrue(isPaired);
    }

    @Test
    fun notPaired() {
        mockCoapGetParing(connection)
        mockCoapGetMeError(connection, 404);
        val isPaired = iamUtil.isCurrentUserPaired(connection)
        assertFalse(isPaired);
    }

    @Test
    fun iamDisabled() {
        mockCoapGetParingError(connection, 404)
        mockCoapGetMeError(connection, 404);
        val exception = assertFailsWith<IamException> {
            iamUtil.isCurrentUserPaired(connection)
        }
        assertEquals(exception.getError(), IamError.IAM_NOT_SUPPORTED);
    }
}
