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
public interface Stream {
    /**
     * Open a stream. This function blocks until the stream is opened.
     *
     * May throw a NabtoRuntimeException with one of the following error codes:
     * ```
     * STOPPED if the stream could not be created, e.g. the handshake is
     * stopped/aborted or the connection or client context is stopped.
     * NOT_CONNECTED if the connection is not established yet.
     * ```
     * 
     * @param streamPort  The streamPort to use on the remote server, a
     * streamPort is a demultiplexing id.
     */
    public void open(int streamPort);

    /**
     * Open a stream without blocking.
     * See Stream.open() for error codes.
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
     * May throw a NabtoRuntimeException with one of the following error codes:
     * ```
     * STOPPED if the stream is stopped.
     * OPERATION_IN_PROGRESS if another read is in progress.
     * ```
     *
     * @return bytes read.
     * @throws NabtoEOFException if eof is reached
     */
    public byte[] readSome() throws NabtoEOFException;

    /**
     * Read some bytes from a stream without blocking.
     * See Stream.readSome() for error codes.
     *
     * @param callback The callback that will be run when the bytes are ready.
     * The callback status will have an EOF status if end of file is reached.
     */
    public void readSomeCallback(NabtoCallback<byte[]> callback) throws NabtoEOFException;

    /**
     * Read an exact amount of bytes from a stream.
     *
     * This function blocks until the bytes is read.
     *
     * May throw a NabtoRuntimeException with one of the following error codes:
     * ```
     * STOPPED if the stream is stopped.
     * OPERATION_IN_PROGRESS if another read is in progress.
     * ```
     * 
     * @param length  The amount of bytes to read.
     * @return Bytes read, less than length bytes can be returned if
     * @throws NabtoEOFException if eof is reached.
     * the stream is reaching end of file.
     */
    public byte[] readAll(int length) throws NabtoEOFException;

    /**
     * Read an exact amount of bytes from a stream without blocking.
     * See Stream.readAll() for error codes.
     *
     * @param length  The amount of bytes to read.
     * @param callback The callback that will be run when the bytes are ready.
     * The callback status will have an EOF status if end of file is reached.
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
     * A call to close does not affect the read direction of the
     * stream.
     * 
     * May throw a NabtoRuntimeException with one of the following error codes:
     * ```
     * STOPPED if the stream is stopped.
     * OPERATION_IN_PROGRESS if a stream close or stream write is in progress.
     * INVALID_STATE if the stream is not yet opened.
     * ```
     */
    public void close();

    /**
     * Close the write direction of the stream without blocking.
     * See Stream.close() for error codes.
     *
     * @param callback The callback that will be run once the stream is closed.
     */
    public void closeCallback(NabtoCallback callback);

    /**
     * Abort a stream. If for some reason the stream just need to be
     * closed, abort will do it for but the write and read direction.
     * 
     * @deprecated use Stream.close()
     */
    public void abort();
}
