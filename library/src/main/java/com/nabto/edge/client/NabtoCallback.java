package com.nabto.edge.client;
import java.util.Optional;

public interface NabtoCallback<T> {
    void run(int errorCode, Optional<T> arg);
}