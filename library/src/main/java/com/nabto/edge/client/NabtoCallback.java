package com.nabto.edge.client;

public interface NabtoCallback<T> {
    void run(int errorCode, T arg);
}