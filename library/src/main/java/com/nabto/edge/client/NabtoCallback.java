package com.nabto.edge.client;

import java.util.Optional;

/**
 * Callback interface for callback based async functions.
 */
public interface NabtoCallback<T> {

    /**
     * Function called when the async operation is completed.
     *
     * Exceptions thrown will be ignored by the caller as the caller has no
     * meaningful way to handle the exception.
     *
     * @param errorCode The resulting error code of the operation.
     * @param arg Argument containing the resulting data if any.
     */
    void run(int errorCode, Optional<T> arg);
}
