package com.nabto.edge.client;

public interface Stream {
    /**
     * Open a stream. This function blocks until the stream is opened.
     *
     * @param streamPort  The streamPort to use on the remote server, a
     * streamPort is a demultiplexing id.
     */
    public void open(int streamPort);

    /**
     * Read some bytes from a stream.
     *
     * This function blocks until stream is read or the stream is
     * closed or end of file. If end of file is reached or the stream
     * is aborted an exception is thrown.
     *
     * @return bytes read.
     */
    public byte[] readSome();

    /**
     * Read an exact amount of bytes from a stream.
     *
     * This function blocks until the bytes is read.
     *
     * @param length  The amount of bytes to read.
     * @return Bytes read, less than length bytes can be returned if
     * the stream is reaching end of file.
     */
    public byte[] readAll(int length);

    /**
     * Write bytes to a stream. This function blocks until the bytes
     * has been written to the stream.
     *
     * @param bytes  The bytes to write to the stream.
     */
    public void write(byte[] bytes);

    /**
     * Close the write direction of the stream. This will make the
     * other end reach end of file when reading from a stream when all
     * sent data has been received and acknowledged.
     *
     * A call to close does not affect the read direction of the
     * stream.
     */
    public void close();

    /**
     * Abort a stream. If for some reason the stream just need to be
     * closed, abort will do it for but the write and read direction.
     */
    public void abort();
}
