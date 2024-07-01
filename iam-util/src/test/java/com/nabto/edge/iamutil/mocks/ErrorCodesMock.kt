package com.nabto.edge.iamutil.mocks

import com.nabto.edge.client.ErrorCodes
import com.nabto.edge.client.swig.Status
import io.mockk.every
import io.mockk.mockkStatic

class ErrorCodesMock {
}

fun mockErrorCodes2() {
    mockErrorCodes()

    mockkStatic(ErrorCodes::class)
    every { ErrorCodes.OK } returns 0
    every { ErrorCodes.ABORTED } returns 1;
    every { ErrorCodes.BAD_RESPONSE } returns 3;
    every { ErrorCodes.CLOSED } returns 5;
    every { ErrorCodes.DNS } returns 6;
    every { ErrorCodes.END_OF_FILE } returns 7;
    every { ErrorCodes.FORBIDDEN } returns 8;
    every { ErrorCodes.FUTURE_NOT_RESOLVED } returns 9;
    every { ErrorCodes.INVALID_ARGUMENT } returns 10;
    every { ErrorCodes.INVALID_STATE } returns 11;
    every { ErrorCodes.NOT_CONNECTED } returns 12;
    every { ErrorCodes.NOT_FOUND } returns 13;
    every { ErrorCodes.NOT_IMPLEMENTED } returns 14;
    every { ErrorCodes.NO_CHANNELS } returns 15;
    every { ErrorCodes.NO_DATA } returns 16;
    every { ErrorCodes.OPERATION_IN_PROGRESS } returns 17;
    every { ErrorCodes.PARSE } returns 18;
    every { ErrorCodes.PORT_IN_USE } returns 19;
    every { ErrorCodes.STOPPED } returns 20;
    every { ErrorCodes.TIMEOUT } returns 21;
    every { ErrorCodes.UNKNOWN } returns 22;
    every { ErrorCodes.NONE } returns 23;
    every { ErrorCodes.NOT_ATTACHED } returns 24;
    every { ErrorCodes.TOKEN_REJECTED } returns 25;
    every { ErrorCodes.COULD_BLOCK } returns 26;
    every { ErrorCodes.UNAUTHORIZED } returns 27;
    every { ErrorCodes.TOO_MANY_REQUESTS } returns 28;
    every { ErrorCodes.UNKNOWN_PRODUCT_ID } returns 29;
    every { ErrorCodes.UNKNOWN_DEVICE_ID } returns 30;
    every { ErrorCodes.UNKNOWN_SERVER_KEY } returns 31;
    every { ErrorCodes.CONNECTION_REFUSED } returns 32;
    every { ErrorCodes.INTERNAL_ERROR } returns 35;
}

fun mockErrorCodes() {
    mockkStatic(Status::class)
    every { Status.getOK() } returns 0;
    every { Status.getABORTED() } returns 1;
    every { Status.getBAD_RESPONSE() } returns 3;
//every { Status.getBAD_REQUEST() } returns 4;
    every { Status.getCLOSED() } returns 5;
    every { Status.getDNS() } returns 6;
    every { Status.getEND_OF_FILE() } returns 7;
    every { Status.getFORBIDDEN() } returns 8;
    every { Status.getFUTURE_NOT_RESOLVED() } returns 9;
    every { Status.getINVALID_ARGUMENT() } returns 10;
    every { Status.getINVALID_STATE() } returns 11;
    every { Status.getNOT_CONNECTED() } returns 12;
    every { Status.getNOT_FOUND() } returns 13;
    every { Status.getNOT_IMPLEMENTED() } returns 14;
    every { Status.getNO_CHANNELS() } returns 15;
    every { Status.getNO_DATA() } returns 16;
    every { Status.getOPERATION_IN_PROGRESS() } returns 17;
    every { Status.getPARSE() } returns 18;
    every { Status.getPORT_IN_USE() } returns 19;
    every { Status.getSTOPPED() } returns 20;
    every { Status.getTIMEOUT() } returns 21;
    every { Status.getUNKNOWN() } returns 22;
    every { Status.getNONE() } returns 23;
    every { Status.getNOT_ATTACHED() } returns 24;
    every { Status.getTOKEN_REJECTED() } returns 25;
    every { Status.getCOULD_BLOCK() } returns 26;
    every { Status.getUNAUTHORIZED() } returns 27;
    every { Status.getTOO_MANY_REQUESTS() } returns 28;
    every { Status.getUNKNOWN_PRODUCT_ID() } returns 29;
    every { Status.getUNKNOWN_DEVICE_ID() } returns 30;
    every { Status.getUNKNOWN_SERVER_KEY() } returns 31;
    every { Status.getCONNECTION_REFUSED() } returns 32;
//every { Status.getDEVICE_INTERNAL_ERROR() } returns 33;
//every { Status.getPRIVILEGED_PORT() } returns 34;
    every { Status.getINTERNAL_ERROR() } returns 35;
}
