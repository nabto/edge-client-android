package com.nabto.edge.iamutil;

/**
 * This enum represents different pairing modes that a device may support.
 * Read more here: https://docs.nabto.com/developer/guides/concepts/iam/pairing.html
 */
public enum PairingMode {
    /**
     * The `local open` pairing mode
     */
    LOCAL_OPEN,
    /**
     * The `local initial` pairing mode
     */
    LOCAL_INITIAL,
    /**
     * The `password open` pairing mode
     */
    PASSWORD_OPEN,
    /**
     * The `password invite` pairing mode
     */
    PASSWORD_INVITE
}
