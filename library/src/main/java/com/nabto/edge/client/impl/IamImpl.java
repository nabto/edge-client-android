package com.nabto.edge.client.impl;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;

import com.nabto.edge.client.*;
import com.nabto.edge.client.Coap.ContentFormat;

public class IamImpl extends Iam {
    Connection connection;

    public IamImpl(Connection connection) {
        this.connection = connection;
    }

    private <T> byte[] encode(T object) {
        ObjectMapper mapper = new CBORMapper();
        byte[] result = {};
        try {
            result = mapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            // @TODO: Log this error
        }
        return result;
    }

    private <T> T decode(byte[] cbor, Class<T> cls) {
        ObjectMapper mapper = new CBORMapper();
        T object = null;
        try {
            object = mapper.readValue(cbor, cls);
        } catch (IOException e) {
            // @TODO: Log this error
        }
        return object;
    }

    private IamException failed() {
        return new IamException(IamError.FAILED);
    }

    private IamException error(IamError error) {
        return new IamException(error);
    }

    public void pairLocalOpen(String desiredUsername) {
        Coap coap = connection.createCoap("POST", "/iam/pairing/local-open");
        IamUser user = new IamUser(desiredUsername);
        coap.setRequestPayload(ContentFormat.APPLICATION_CBOR, encode(user));
        coap.execute();
        int status = coap.getResponseStatusCode();
        switch (status) {
            case 201: break;
            case 400: throw error(IamError.INVALID_INPUT);
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            case 404: throw error(IamError.PAIRING_MODE_DISABLED);
            case 409: throw error(IamError.USERNAME_EXISTS);
            default: throw failed();
        }
    }

    public void pairLocalInitial() {
        Coap coap = connection.createCoap("POST", "/iam/pairing/local-initial");
        coap.execute();
        int status = coap.getResponseStatusCode();
        switch (status) {
            case 201: break;
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            case 404: throw error(IamError.PAIRING_MODE_DISABLED);
            case 409: throw error(IamError.INITIAL_USER_ALREADY_PAIRED);
            default: throw failed();
        }
    }

    public void pairPasswordOpen(String desiredUsername, String password) {
        byte[] cbor = encode(new IamUser(desiredUsername));
        connection.passwordAuthenticate("", password);
        Coap coap = connection.createCoap("POST", "/iam/pairing/password-open");
        coap.setRequestPayload(ContentFormat.APPLICATION_CBOR, cbor);
        coap.execute();
        int status = coap.getResponseStatusCode();
        switch (status) {
            case 201: break;
            case 400: throw error(IamError.INVALID_INPUT);
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            case 404: throw error(IamError.PAIRING_MODE_DISABLED);
            case 409: throw error(IamError.USERNAME_EXISTS);
            default: throw failed();
        }
    }

    public void pairPasswordInvite(String username, String password) {
        connection.passwordAuthenticate(username, password);
        Coap coap = connection.createCoap("POST", "/iam/pairing/password-invite");
        coap.execute();
        int status = coap.getResponseStatusCode();
        switch (status) {
            case 201: break;
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            case 404: throw error(IamError.PAIRING_MODE_DISABLED);
            default: throw failed();
        }
    }

    public PairingMode[] getAvailablePairingModes() {
        IamDeviceDetails details = getDeviceDetails();
        ArrayList<PairingMode> list = new ArrayList<>();
        for (String stringMode : details.getModes()) {
            PairingMode mode; 
            switch (stringMode) {
                case "LocalInitial": mode = PairingMode.LOCAL_INITIAL; break;
                case "LocalOpen": mode = PairingMode.LOCAL_OPEN; break;
                case "PasswordInvite": mode = PairingMode.PASSWORD_INVITE; break;
                case "PasswordOpen": mode = PairingMode.PASSWORD_OPEN; break;
                default: throw failed();
            }
            list.add(mode);
        }
        return list.toArray(new PairingMode[list.size()]);
    }

    public IamDeviceDetails getDeviceDetails() {
        Coap coap = connection.createCoap("GET", "/iam/pairing");
        coap.execute();
        int status = coap.getResponseStatusCode();
        switch (status) {
            case 205: break;
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            case 404: throw error(IamError.IAM_NOT_SUPPORTED);
            default: throw failed();
        }
        IamDeviceDetails details = decode(coap.getResponsePayload(), IamDeviceDetails.class);
        return details;
    }

    public boolean isCurrentUserPaired() {
        Coap coap = connection.createCoap("GET", "/iam/me");
        coap.execute();
        int status = coap.getResponseStatusCode();
        switch (status) {
            case 205: return true;
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            case 404: return false;
            default: throw failed();
        }
    }

    public IamUser getUser(String username) {
        String path = String.format("/iam/users/%s", username);
        Coap coap = connection.createCoap("GET", path);
        coap.execute();
        int status = coap.getResponseStatusCode();
        switch (status) {
            case 205: break;
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            case 404: throw error(IamError.USER_DOES_NOT_EXIST);
            default: throw failed();
        }
        IamUser user = decode(coap.getResponsePayload(), IamUser.class);
        return user;
    }

    public IamUser getCurrentUser() {
        Coap coap = connection.createCoap("GET", "/iam/me");
        coap.execute();
        int status = coap.getResponseStatusCode();
        switch (status) {
            case 205: break;
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            case 404: throw error(IamError.USER_DOES_NOT_EXIST);
            default: throw failed();
        }
        IamUser user = decode(coap.getResponsePayload(), IamUser.class);
        return user;
    }

    private void updateUser(String username, String key, String value, IamError error404) {
        String path = String.format("/iam/users/%s/%s", username, key);
        byte[] cbor = encode(value);
        Coap coap = connection.createCoap("PUT", path);
        coap.setRequestPayload(ContentFormat.APPLICATION_CBOR, cbor);
        coap.execute();
        int status = coap.getResponseStatusCode();
        switch (status) {
            case 204: break;
            case 400: throw error(IamError.INVALID_INPUT);
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            // @TODO: 404 has different semantics depending on what went wrong
            //        E.g. it could mean the user doesnt exist, the role doesnt exist...
            //        We have two errors USER_DOES_NOT_EXIST and ROLE_DOES_NOT_EXIST
            //        and we can only report one. Shouldn't they just be one error?
            //        such as INVALID_USER_UPDATE_SETTINGS or something.
            case 404: throw error(error404);
            default: throw failed();
        }
    }

    public void createUser(String username, String password, String role) {
        Coap coap = connection.createCoap("POST", "/iam/users");
        IamUser user = new IamUser(username);
        coap.setRequestPayload(ContentFormat.APPLICATION_CBOR, encode(user));
        coap.execute();
        int status = coap.getResponseStatusCode();
        switch (status) {
            case 201: break;
            case 400: throw error(IamError.INVALID_INPUT);
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            case 409: throw error(IamError.USERNAME_EXISTS);
            default: throw failed();
        }
        updateUser(username, "password", password, IamError.USER_DOES_NOT_EXIST);
        updateUser(username, "role", role, IamError.ROLE_DOES_NOT_EXIST);
    }

    public void updateUserPassword(String username, String password) {
        updateUser(username, "password", password, IamError.USER_DOES_NOT_EXIST);
    }
    
    public void updateUserRole(String username, String role) {
        updateUser(username, "role", role, IamError.ROLE_DOES_NOT_EXIST);
    }

    public void updateUserDisplayName(String username, String displayName) {
        updateUser(username, "display-name", displayName, IamError.USER_DOES_NOT_EXIST);
    }

    public void renameUser(String username, String newUsername) {
        updateUser(username, "username", newUsername, IamError.USER_DOES_NOT_EXIST);
    }

    public void deleteUser(String username) {
        String path = "/iam/users/" + username;
        Coap coap = connection.createCoap("DELETE", path);
        coap.execute();
        switch (coap.getResponseStatusCode()) {
            case 202: break;
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            case 404: throw error(IamError.USER_DOES_NOT_EXIST);
            default: throw failed();
        }
    }
}