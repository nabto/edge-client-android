package com.nabto.edge.iamutil

import com.nabto.edge.client.Connection
import com.nabto.edge.iamutil.mocks.createCoapErrorMock
import com.nabto.edge.iamutil.mocks.createGetPairingCoapMock
import com.nabto.edge.iamutil.mocks.createPutUserUsernameCoapMock
import com.nabto.edge.iamutil.mocks.pairingLocalInitialCoapMock
import com.nabto.edge.iamutil.mocks.pairingLocalOpenCoapMock
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

@kotlinx.serialization.ExperimentalSerializationApi
class PairLocalInitialTest {
    val connection : Connection = mockk<Connection>();
    val iamUtil = IamUtil.create()
    val username = "testuser"

    @Before
    fun setup() {
        every { connection.createCoap("GET", "/iam/pairing" ) } returns ( createGetPairingCoapMock() )
    }

    @Test
    fun pairLocalInitial() {
        every { connection.createCoap("POST", "/iam/pairing/local-initial" ) } returns ( pairingLocalInitialCoapMock(201) )
        iamUtil.pairLocalInitial(connection);
    }

    @Test
    fun pairLocalinitialBlockedByIam() {
        every { connection.createCoap("POST", "/iam/pairing/local-initial" ) } returns ( pairingLocalInitialCoapMock(403) )
        val exception = assertFailsWith<IamException> {
            iamUtil.pairLocalInitial(connection)
        }
        assertEquals(exception.getError(), IamError.BLOCKED_BY_DEVICE_CONFIGURATION)
    }
    @Test
    fun pairLocalInitialModeDisabled() {
        every { connection.createCoap("POST", "/iam/pairing/local-initial" ) } returns ( pairingLocalInitialCoapMock(404) )
        val exception = assertFailsWith<IamException> {
            iamUtil.pairLocalInitial(connection)
        }
        assertEquals(exception.getError(), IamError.PAIRING_MODE_DISABLED)
    }
    @Test
    fun pairLocalInitialAlreadyPaired() {
        every { connection.createCoap("POST", "/iam/pairing/local-initial" ) } returns ( pairingLocalInitialCoapMock(409) )
        val exception = assertFailsWith<IamException> {
            iamUtil.pairLocalInitial(connection)
        }
        assertEquals(exception.getError(), IamError.INITIAL_USER_ALREADY_PAIRED)
    }
}
