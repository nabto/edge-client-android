package com.nabto.edge.client;

/**
 * The Stream API.
 *
 * The Streaming API enables socket-like communication between client and device. The stream is
 * reliable and ensures data is received ordered and complete. If either of these conditions cannot be
 * met, the stream will be closed in such a way that it is detectable.
 *
 * Stream instances are created using the Connection.createStream() factory method.
 * The Stream object must be kept alive while in use.
 *
 */
public interface Stream extends AutoCloseable {
    /**
     * Open a stream. This function blocks until the stream is opened.
     *
     * @param streamPort  The streamPort to use on the remote server, a
     * streamPort is a demultiplexing id.
     * @throws NabtoRuntimeException with error code `STOPPED` if the stream or a parent object was stopped.
     * @throws NabtoRuntimeException with error code `NOT_CONNECTED` if the connection is not established yet.
     */
    public void open(int streamPort);

    /**
     * Open a stream without blocking.
     * See Stream.open() for possible callback error codes.
     *
     * @param streamPort  The streamPort to use on the remote server, a
     * streamPort is a demultiplexing id.
     * @param callback The callback that will be run once the stream is opened.
     */
    public void openCallback(int streamPort, NabtoCallback callback);

    /**
     * Read some bytes from a stream.
     *
     * This function blocks until stream is read or the stream is
     * closed or end of file. If end of file is reached or the stream
     * is aborted an exception is thrown.
     *
     * @throws NabtoRuntimeException With error code `STOPPED` if the stream was stopped.
     * @throws NabtoRuntimeException With error code `OPERATION_IN_PROGRESS` if another read is in progress.
     * @throws NabtoEOFException if eof is reached
     * @return bytes read.
     */
    public byte[] readSome() throws NabtoEOFException;

    /**
     * Read some bytes from a stream without blocking.
     * See Stream.readSome() for possible error codes.
     *
     * @param callback The callback that will be run when the bytes are ready.
     */
    public void readSomeCallback(NabtoCallback<byte[]> callback) throws NabtoEOFException;

    /**
     * Read an exact amount of bytes from a stream.
     *
     * This function blocks until the bytes is read.
     *
     * @param length  The amount of bytes to read.
     * @throws NabtoRuntimeException With error code `STOPPED` if the stream was stopped.
     * @throws NabtoRuntimeException With error code `OPERATION_IN_PROGRESS` if another read is in progress.
     * @throws NabtoEOFException if end of file is reached.
     * @return Bytes read, less than length bytes can be returned if
     */
    public byte[] readAll(int length) throws NabtoEOFException;

    /**
     * Read an exact amount of bytes from a stream without blocking.
     * See Stream.readAll() for possible error codes.
     *
     * @param length  The amount of bytes to read.
     * @param callback The callback that will be run when the bytes are ready.
     * The callback status will have an EOF status if end of file is reached.
     * @throws NabtoEOFException if end of file is reached.
     */
    public void readAllCallback(int length, NabtoCallback<byte[]> callback) throws NabtoEOFException;

    /**
     * Write bytes to a stream. This function blocks until the bytes
     * has been written to the stream.
     *
     * @param bytes  The bytes to write to the stream.
     */
    public void write(byte[] bytes);

    /**
     * Write bytes to a stream without blocking.
     *
     * @param bytes  The bytes to write to the stream.
     * @param callback The callback that will be run once the operation is done.
     */
    public void writeCallback(byte[] bytes, NabtoCallback callback);

    /**
     * Close the write direction of the stream. This will make the
     * other end reach end of file when reading from a stream when all
     * sent data has been received and acknowledged.
     *
     * A call to this function does not affect the read direction of the
     * stream.
     *
     * Note: Odd name is to distinguish from AutoCloseable's close() with very different
     * semantics.
     *
     * @throws NabtoRuntimeException With error code `STOPPED` if the stream is stopped.
     * @throws NabtoRuntimeException With error code `OPERATION_IN_PROGRESS` if another stop is in progress.
     * @throws NabtoRuntimeException With error code `INVALID_STATE` if the stream is not opened yet.
     */
    public void streamClose();

    /**
     * Close the write direction of the stream without blocking.
     * See Stream.close() for error codes.
     *
     * Note: Odd name is to distinguish from AutoCloseable's close() with very different
     * semantics.
     *
     * @param callback The callback that will be run once the stream is closed.
     */
    public void streamCloseCallback(NabtoCallback callback);

    /**
     * Note! Semantics have changed with version 3.0 due to name clash with AutoCloseable!
     * The 2.x version of close() has been renamed to streamClose().
     *
     * This function releases any resources associated with the Stream instance. This method is
     * called automatically at the end of a try-with-resources block, which
     * helps to ensure that resources are released promptly and reliably.
     *
     * <p>Example of using a CoAP object within a try-with-resources statement:</p>
     * <pre>
     * try (Stream stream = connection.createStream(...)) {
     *     // ... use stream
     * }
     * </pre>
     *
     * <p>With this setup, {@code close()} will be called automatically on
     * {@code connect} at the end of the block, releasing any underlying
     * native Nabto Client SDK resources without any further action required on the application.</p>
     *
     * <p></p>If the try-with-resources construct is not feasible, the application must manually call close()
     * when the Stream instance is no longer needed.</p>
     *
     * <p>Unlike the {@link AutoCloseable#close()} method, this {@code close()}
     * method does not throw any exceptions.</p>
     */
    @Override
    void close();

    /**
     * @deprecated use Stream.close()
     *
     * Abort a stream. If for some reason the stream just need to be
     * closed, abort will do it for but the write and read direction.
     */
    public void abort();

}
