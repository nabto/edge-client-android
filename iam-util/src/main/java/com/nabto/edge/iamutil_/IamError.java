package com.nabto.edge.iamutil;

/**
 * IAM Util specific error codes.
 */
public enum IamError {
    /**
     * No error, mainly used in IamCallback
     */
    NONE,

    /**
     * The device configuration does not support the requested operation.
     */
    BLOCKED_BY_DEVICE_CONFIGURATION,

    /**
     * If the initial user was already paired when attempting to perform an initial pairing scenario.
     */
    INITIAL_USER_ALREADY_PAIRED,

    /**
     * The operation referenced a user on the device that does not exist.
     */
    USER_DOES_NOT_EXIST,

    /**
     * The operation referenced a role on the device that does not exist.
     */
    ROLE_DOES_NOT_EXIST,

    /**
     * The operation requested creating a user or assigning a username that was already in use on the device.
     */
    USERNAME_EXISTS,

    /**
     * Specified input is invalid on the device, see the specific endpoint document for restrictions on input, e.g.
     * https://docs.nabto.com/developer/api-reference/coap/iam/post-users.html#request
     */
    INVALID_INPUT,

    /**
     * The requested pairing mode is disabled on the device.
     */
    PAIRING_MODE_DISABLED,

    /**
     * The embedded device does not provide a Nabto Edge IAM implementation.
     */
    IAM_NOT_SUPPORTED,

    /**
     * The client could be authenticated towards the device using the specified credentials.
     */
    AUTHENTICATION_ERROR,

    /**
     * Something unspecified failed.
     */
    FAILED
}
