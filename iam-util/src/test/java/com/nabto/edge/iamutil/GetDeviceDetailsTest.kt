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
import io.mockk.core.ValueClassSupport.maybeUnboxValueForMethodReturn
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
class GetDeviceDetailsTest {
    val connection : Connection = mockk<Connection>();
    val iamUtil = IamUtil.create()

    @Before
    fun setup() {
        mockErrorCodes()
    }

    @Test
    fun okEmptyResponse() {
        mockCoapGetParing(connection, PairingResponse())
        val details = iamUtil.getDeviceDetails(connection)
        assertEquals(details.deviceId, null)
    }

    @Test
    fun okAllData() {
        val pr = PairingResponse(
            Modes = arrayOf("LocalInitial", "LocalOpen", "PasswordOpen", "PasswordInitial"),
            NabtoVersion = "nabtoVersion",
            AppVersion = "appVersion",
            AppName = "appName",
            DeviceId = "deviceId",
            ProductId = "productId",
            FriendlyName = "friendlyName",
            Unknown = "extradata")
        mockCoapGetParing(connection, pr);
        val details = iamUtil.getDeviceDetails(connection)

        assertEquals(4, details.modes.size)
        assertEquals(pr.NabtoVersion, details.nabtoVersion);
        assertEquals(pr.AppVersion, details.appVersion);
        assertEquals(pr.AppName, details.appName);
        assertEquals(pr.DeviceId, details.deviceId);
        assertEquals(pr.ProductId, details.productId);
    }
}
