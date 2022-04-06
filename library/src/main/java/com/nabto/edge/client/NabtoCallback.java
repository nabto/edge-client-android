package com.nabto.edge.client;

public interface NabtoCallback<T> {
    void run(T arg);
}