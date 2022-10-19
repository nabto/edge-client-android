package com.nabto.edge.client;

/**
 * This class represents error codes that the nabto client sdk exposes. The documentation for the
 * actual error codes varies slightly with context, so see the detailed documentation for the Nabto
 * Client SDK for details where the codes can be returned.
 */
public class ErrorCodes {

    /**
     * Operation was successful.
     */
    public static int OK = com.nabto.edge.client.swig.Status.getOK();

    /**
     * Operation was aborted.
     */
    public static int ABORTED = com.nabto.edge.client.swig.Status.getABORTED();

    /**
     * Unexpected or wrongly formatted response from remote peer.
     */
    public static int BAD_RESPONSE = com.nabto.edge.client.swig.Status.getBAD_RESPONSE();

    /**
     * Connection is closed.
     */
    public static int CLOSED = com.nabto.edge.client.swig.Status.getCLOSED();

    /**
     * A DNS related error occurred.
     */
    public static int DNS = com.nabto.edge.client.swig.Status.getDNS();

    /**
     * End-of-file was reached
     */
    public static int END_OF_FILE = com.nabto.edge.client.swig.Status.getEND_OF_FILE();

    /**
     * The current user has been rejected access to the requested resource.
     */
    public static int FORBIDDEN =  com.nabto.edge.client.swig.Status.getFORBIDDEN();

    /**
     * The queried future is not resolved yet.
     */
    public static int FUTURE_NOT_RESOLVED =  com.nabto.edge.client.swig.Status.getFUTURE_NOT_RESOLVED();

    /**
     * Invalid argument specified.
     */
    public static int INVALID_ARGUMENT =  com.nabto.edge.client.swig.Status.getINVALID_ARGUMENT();

    /**
     * A function was invoked on the SDK when it was not in an appropriate state, for instance
     * setting configuration parameters on a connection after it is connected.
     */
    public static int INVALID_STATE =  com.nabto.edge.client.swig.Status.getINVALID_STATE();

    /**
     * The connection failed for some unspecified reason.
     */
    public static int NOT_CONNECTED =  com.nabto.edge.client.swig.Status.getNOT_CONNECTED();

    /**
     * An entity was not found (e.g., no hosts found during discovery).
     */
    public static int NOT_FOUND =  com.nabto.edge.client.swig.Status.getNOT_FOUND();

    /**
     * Functionality not implemented.
     */
    public static int NOT_IMPLEMENTED =  com.nabto.edge.client.swig.Status.getNOT_IMPLEMENTED();

    /**
     * It was not possible to open any channel (p2p, relay, direct) to the target device, ie no
     * connection could be established.
     */
    public static int NO_CHANNELS =  com.nabto.edge.client.swig.Status.getNO_CHANNELS();

    /**
     * No data received.
     */
    public static int NO_DATA =  com.nabto.edge.client.swig.Status.getNO_DATA();

    /**
     * Another operation is in progress that may prevent the desired function to complete
     * successfully.
     */
    public static int OPERATION_IN_PROGRESS =  com.nabto.edge.client.swig.Status.getOPERATION_IN_PROGRESS();

    /**
     * Parse error.
     */
    public static int PARSE =  com.nabto.edge.client.swig.Status.getPARSE();

    /**
     * Requested port is already use.
     */
    public static int PORT_IN_USE =  com.nabto.edge.client.swig.Status.getPORT_IN_USE();

    /**
     * The requested entity has been stopped.
     */
    public static int STOPPED =  com.nabto.edge.client.swig.Status.getSTOPPED();

    /**
     * A timeout occurred before the operation completed.
     */
    public static int TIMEOUT =  com.nabto.edge.client.swig.Status.getTIMEOUT();

    /**
     * An unknown error occured.
     */
    public static int UNKNOWN =  com.nabto.edge.client.swig.Status.getUNKNOWN();

    /**
     * Always depends on context.
     */
    public static int NONE = com.nabto.edge.client.swig.Status.getNONE();

    /**
     * The target device is not attached to any server (it is offline).
     */
    public static int NOT_ATTACHED = com.nabto.edge.client.swig.Status.getNOT_ATTACHED();

    /**
     * The basestation rejected a connection due to invalid SCT or JWT
     */
    public static int TOKEN_REJECTED = com.nabto.edge.client.swig.Status.getTOKEN_REJECTED();

    /**
     * An operation would have blocked a thread that should not be blocked
     */
    public static int COULD_BLOCK = com.nabto.edge.client.swig.Status.getCOULD_BLOCK();

    /**
     * A request was did not have proper authorization
     */
    public static int UNAUTHORIZED = com.nabto.edge.client.swig.Status.getUNAUTHORIZED();

    /**
     * A request was rejected due to rate limiting
     */
    public static int TOO_MANY_REQUESTS = com.nabto.edge.client.swig.Status.getTOO_MANY_REQUESTS();

    /**
     * The basestation rejected a connection due to unknown product ID
     */
    public static int UNKNOWN_PRODUCT_ID = com.nabto.edge.client.swig.Status.getUNKNOWN_PRODUCT_ID();

    /**
     * The basestation rejected a connection due to unknown device ID
     */
    public static int UNKNOWN_DEVICE_ID = com.nabto.edge.client.swig.Status.getUNKNOWN_DEVICE_ID();

    /**
     * The basestation rejected a connection due to unknown server key
     */
    public static int UNKNOWN_SERVER_KEY = com.nabto.edge.client.swig.Status.getUNKNOWN_SERVER_KEY();

    /**
     * A connection was refused by the network
     */
    public static int CONNECTION_REFUSED = com.nabto.edge.client.swig.Status.getCONNECTION_REFUSED();

    /**
     * An internal error occured
     */
    public static int INTERNAL_ERROR = com.nabto.edge.client.swig.Status.getINTERNAL_ERROR();
}
