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
import com.nabto.edge.iamutil.mocks.mockCoapGetPairingError
import com.nabto.edge.iamutil.mocks.mockCoapGetParing
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
class GetAvailablePairingModesTest {
    val connection : Connection = mockk<Connection>();
    val iamUtil = IamUtil.create()
    val username = "testuser"
    val password = "testpassword"

    @Before
    fun setup() {
        mockErrorCodes()
    }

    @Test
    fun ok() {
        mockCoapGetParing(connection, PairingResponse(Modes = arrayOf("LocalInitial")))
        val pairingModes = iamUtil.getAvailablePairingModes(connection)
        assertEquals(pairingModes.size, 1);
        assertEquals(pairingModes[0], PairingMode.LOCAL_INITIAL);
    }

    /*@Test
    fun handleUnknownMode() {
        mockCoapGetParing(connection, PairingResponse(Modes = arrayOf("LocalOpen", "Unknown")))
        val pairingModes = iamUtil.getAvailablePairingModes(connection)
        assertEquals(pairingModes.size, 0);
    }*/

    @Test
    fun handleUnknownFieldInCoapResponse() {
        mockCoapGetParing(connection, PairingResponse(Unknown = "unknown"))
        val pairingModes = iamUtil.getAvailablePairingModes(connection)
        assertEquals(pairingModes.size, 0);
    }

    @Test
    fun handle404() {
        mockCoapGetPairingError(connection, 404)
        val exception = assertFailsWith<IamException> {
            val pairingModes = iamUtil.getAvailablePairingModes(connection)
        }
        assertEquals(exception.getError(), IamError.IAM_NOT_SUPPORTED)
    }
    @Test
    fun handle403() {
        mockCoapGetPairingError(connection, 403)
        val exception = assertFailsWith<IamException> {
            val pairingModes = iamUtil.getAvailablePairingModes(connection)
        }
        assertEquals(exception.getError(), IamError.BLOCKED_BY_DEVICE_CONFIGURATION)
    }
}
