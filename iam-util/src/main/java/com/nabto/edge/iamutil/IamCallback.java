package com.nabto.edge.iamutil;
import java.util.Optional;

/**
 * Callback interface for callback based async functions.
 */
public interface IamCallback<T> {
    /**
     * Function called when the async operation is completed.
     *
     * @param error The resulting error code of the operation.
     * @param arg Argument containing the resulting data if any.
     */
    void run(IamError error, Optional<T> arg);
}
