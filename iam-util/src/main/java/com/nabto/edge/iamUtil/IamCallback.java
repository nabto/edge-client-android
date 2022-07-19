package com.nabto.edge.iamutil;
import java.util.Optional;

public interface IamCallback<T> {
    void run(IamError error, Optional<T> arg);
}
