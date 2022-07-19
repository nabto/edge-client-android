package com.nabto.edge.iamutil.impl;

import java.io.IOException;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

import com.nabto.edge.iamutil.*;
import com.nabto.edge.client.Coap.ContentFormat;
import com.nabto.edge.client.Coap;
import com.nabto.edge.client.Connection;

public class IamImplUtil {
    public static byte[] encode(Object object) {
        ObjectMapper mapper = new CBORMapper();
        byte[] result = {};
        try {
            result = mapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new IamException(IamError.FAILED, "CBOR serialization failed.");
        }
        return result;
    }

    public static <T> T decode(byte[] cbor, Class<T> cls) {
        ObjectMapper mapper = new CBORMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        T object = null;
        try {
            object = mapper.readValue(cbor, cls);
        } catch (IOException e) {
            throw new IamException(IamError.FAILED, "CBOR deserialization failed.");
        }
        return object;
    }

    public static Coap createCoap(Connection connection, IamPath path, String... strings) {
        switch (path) {
            case PAIR_LOCAL_OPEN:
                return connection.createCoap("POST", "/iam/pairing/local-open");

            case PAIR_LOCAL_INITIAL: 
                return connection.createCoap("POST", "/iam/pairing/local-initial");

            case PAIR_PASSWORD_OPEN:
                return connection.createCoap("POST", "/iam/pairing/password-open");

            case PAIR_PASSWORD_INVITE:
                return connection.createCoap("POST", "/iam/pairing/password-invite");
            
            case GET_DEVICE_DETAILS:
                return connection.createCoap("GET", "/iam/pairing");

            case GET_ME:
                return connection.createCoap("GET", "/iam/me");

            case GET_USER:
                return connection.createCoap("GET", String.format("/iam/users/%s", strings[0]));

            case CREATE_USER:
                return connection.createCoap("POST", "/iam/users");

            case UPDATE_USER:
                return connection.createCoap("PUT", String.format("/iam/users/%s/%s", strings[0], strings[1]));

            case DELETE_USER:
                return connection.createCoap("DELETE", String.format("/iam/users/%s", strings[0]));

            default: throw new IllegalArgumentException("Enum switch is not exhaustive");
        }
    }
}