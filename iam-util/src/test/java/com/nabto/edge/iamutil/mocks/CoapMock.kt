package com.nabto.edge.iamutil.mocks

import com.nabto.edge.client.Coap
import com.nabto.edge.client.ErrorCode
import com.nabto.edge.client.ErrorCodes
import com.nabto.edge.client.NabtoCallback
import com.nabto.edge.client.NabtoException
import com.nabto.edge.client.NabtoRuntimeException
import io.mockk.every
import io.mockk.mockk
import java.util.Optional

fun createCoapMock() : Coap {
    val coap : Coap = mockk<Coap>();
    every { coap.close() } returns ( Unit )
    every { coap.execute() } returns ( Unit )
    every { coap.executeCallback(any()) } answers {
        val callback = firstArg<NabtoCallback<Void>>();
        callback.run(0, Optional.empty<Void>());
    }

    return coap;
}

fun createCoapErrorMock(statusCode : Int) : Coap {
    val coap : Coap = createCoapMock();
    every { coap.responseStatusCode } returns ( statusCode )
    every { coap.setRequestPayload(any(), any()) } returns ( Unit )
    return coap
}

fun createCoapExecuteErrorMock(errorCodeInt : Int) : Coap {
    val coap : Coap = mockk<Coap>();
    every { coap.close() } returns ( Unit )
    val runtimeException = mockk<NabtoRuntimeException>();
    //every { runtimeException.cause } returns null
    val errorCode = mockk<ErrorCode>()
    every { errorCode.errorCode } returns errorCodeInt

    every { runtimeException.errorCode } returns errorCode
    every { coap.execute() } throws (runtimeException)
    every { coap.executeCallback(any()) } answers {
        val callback = firstArg<NabtoCallback<Void>>();
        callback.run(errorCodeInt, Optional.empty<Void>());
    }

    return coap;
}