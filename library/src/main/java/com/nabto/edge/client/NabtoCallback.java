package com.nabto.edge.client;

import java.util.Optional;

/**
 * Callback interface for callback based async functions.
 */
public interface NabtoCallback<T> {

    /**
     * Function called when the async operation is completed.
     *
     * @param errorCode The resulting error code of the operation.
     * @param arg Argument containing the resulting data if any.
     */
    void run(int errorCode, Optional<T> arg);
}
