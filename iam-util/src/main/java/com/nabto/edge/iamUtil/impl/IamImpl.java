package com.nabto.edge.iamUtil.impl;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

import com.nabto.edge.iamUtil.*;
import com.nabto.edge.client.Coap.ContentFormat;
import com.nabto.edge.client.Coap;
import com.nabto.edge.client.NabtoRuntimeException;
import com.nabto.edge.client.ErrorCodes;
import com.nabto.edge.client.Connection;

public class IamImpl extends Iam {
    public IamImpl() { }

    private <T> byte[] encode(T object) {
        ObjectMapper mapper = new CBORMapper();
        byte[] result = {};
        try {
            result = mapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new IamException(IamError.FAILED, "CBOR serialization failed.");
        }
        return result;
    }

    private <T> T decode(byte[] cbor, Class<T> cls) {
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

    private int execute(Coap coap, Connection connection) {
        coap.execute();
        int status = coap.getResponseStatusCode();
        if (status == 404) {
            // Probe /iam/pairing to see if the device supports IAM
            Coap probe = connection.createCoap("GET", "/iam/pairing");
            probe.execute();
            int probeStatus = probe.getResponseStatusCode();
            if (probeStatus != 205 && probeStatus != 403) {
                throw error(IamError.IAM_NOT_SUPPORTED);
            }
        }
        return status;
    }

    private IamException failed(int status) {
        return new IamException(IamError.FAILED, "got unexpected status code " + status);
    }

    private IamException error(IamError error) {
        return new IamException(error);
    }

    public void pairLocalOpen(Connection connection, String desiredUsername) {
        Coap coap = connection.createCoap("POST", "/iam/pairing/local-open");
        IamUser user = new IamUser(desiredUsername);
        coap.setRequestPayload(ContentFormat.APPLICATION_CBOR, encode(user));
        int status = execute(coap, connection);
        switch (status) {
            case 201: break;
            case 400: throw error(IamError.INVALID_INPUT);
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            case 404: throw error(IamError.PAIRING_MODE_DISABLED);
            case 409: throw error(IamError.USERNAME_EXISTS);
            default: throw failed(status);
        }
    }

    public void pairLocalInitial(Connection connection) {
        Coap coap = connection.createCoap("POST", "/iam/pairing/local-initial");
        int status = execute(coap, connection);
        switch (status) {
            case 201: break;
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            case 404: throw error(IamError.PAIRING_MODE_DISABLED);
            case 409: throw error(IamError.INITIAL_USER_ALREADY_PAIRED);
            default: throw failed(status);
        }
    }

    private void passwordAuthenticate(Connection connection, String username, String password) {
        try {
            connection.passwordAuthenticate(username, password);
        } catch (NabtoRuntimeException e) {
            if (e.getErrorCode().getErrorCode() == ErrorCodes.UNAUTHORIZED) {
                throw error(IamError.AUTHENTICATION_ERROR);
            } else {
                throw e;
            }
        }
    }

    public void pairPasswordOpen(Connection connection, String desiredUsername, String password) {
        byte[] cbor = encode(new IamUser(desiredUsername));
        passwordAuthenticate(connection, "", password);
        Coap coap = connection.createCoap("POST", "/iam/pairing/password-open");
        coap.setRequestPayload(ContentFormat.APPLICATION_CBOR, cbor);
        int status = execute(coap, connection);
        switch (status) {
            case 201: break;
            case 400: throw error(IamError.INVALID_INPUT);
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            case 404: throw error(IamError.PAIRING_MODE_DISABLED);
            case 409: throw error(IamError.USERNAME_EXISTS);
            default: throw failed(status);
        }
    }

    public void pairPasswordInvite(Connection connection, String username, String password) {
        passwordAuthenticate(connection, username, password);
        Coap coap = connection.createCoap("POST", "/iam/pairing/password-invite");
        int status = execute(coap, connection);
        switch (status) {
            case 201: break;
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            case 404: throw error(IamError.PAIRING_MODE_DISABLED);
            default: throw failed(status);
        }
    }

    public PairingMode[] getAvailablePairingModes(Connection connection) {
        IamDeviceDetails details = getDeviceDetails(connection);
        ArrayList<PairingMode> list = new ArrayList<>();
        for (String stringMode : details.getModes()) {
            PairingMode mode;
            switch (stringMode) {
                case "LocalInitial": mode = PairingMode.LOCAL_INITIAL; break;
                case "LocalOpen": mode = PairingMode.LOCAL_OPEN; break;
                case "PasswordInvite": mode = PairingMode.PASSWORD_INVITE; break;
                case "PasswordOpen": mode = PairingMode.PASSWORD_OPEN; break;
                default: throw new IamException(IamError.FAILED, "unknown mode " + stringMode);
            }
            list.add(mode);
        }
        return list.toArray(new PairingMode[list.size()]);
    }

    public IamDeviceDetails getDeviceDetails(Connection connection) {
        Coap coap = connection.createCoap("GET", "/iam/pairing");
        // Don't use Iam.execute() here because it's redundant.
        coap.execute();
        int status = coap.getResponseStatusCode();
        switch (status) {
            case 205: break;
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            case 404: throw error(IamError.IAM_NOT_SUPPORTED);
            default: throw failed(status);
        }
        IamDeviceDetails details = decode(coap.getResponsePayload(), IamDeviceDetails.class);
        return details;
    }

    public boolean isCurrentUserPaired(Connection connection) {
        Coap coap = connection.createCoap("GET", "/iam/me");
        int status = execute(coap, connection);
        switch (status) {
            case 205: return true;
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            case 404: return false;
            default: throw failed(status);
        }
    }

    public IamUser getUser(Connection connection, String username) {
        String path = String.format("/iam/users/%s", username);
        Coap coap = connection.createCoap("GET", path);
        int status = execute(coap, connection);
        switch (status) {
            case 205: break;
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            case 404: throw error(IamError.USER_DOES_NOT_EXIST);
            default: throw failed(status);
        }
        IamUser user = decode(coap.getResponsePayload(), IamUser.class);
        return user;
    }

    public IamUser getCurrentUser(Connection connection) {
        Coap coap = connection.createCoap("GET", "/iam/me");
        int status = execute(coap, connection);
        switch (status) {
            case 205: break;
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            case 404: throw error(IamError.USER_DOES_NOT_EXIST);
            default: throw failed(status);
        }
        IamUser user = decode(coap.getResponsePayload(), IamUser.class);
        return user;
    }

    private void updateUser(Connection connection, String username, String key, String value, IamError error404) {
        String path = String.format("/iam/users/%s/%s", username, key);
        byte[] cbor = encode(value);
        Coap coap = connection.createCoap("PUT", path);
        coap.setRequestPayload(ContentFormat.APPLICATION_CBOR, cbor);
        int status = execute(coap, connection);
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
            default: throw failed(status);
        }
    }

    public void createUser(Connection connection, String username, String password, String role) {
        Coap coap = connection.createCoap("POST", "/iam/users");
        IamUser user = new IamUser(username);
        coap.setRequestPayload(ContentFormat.APPLICATION_CBOR, encode(user));
        int status = execute(coap, connection);
        switch (status) {
            case 201: break;
            case 400: throw error(IamError.INVALID_INPUT);
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            case 409: throw error(IamError.USERNAME_EXISTS);
            default: throw failed(status);
        }
        updateUser(connection, username, "password", password, IamError.USER_DOES_NOT_EXIST);
        updateUser(connection, username, "role", role, IamError.ROLE_DOES_NOT_EXIST);
    }

    public void updateUserPassword(Connection connection, String username, String password) {
        updateUser(connection, username, "password", password, IamError.USER_DOES_NOT_EXIST);
    }

    public void updateUserRole(Connection connection, String username, String role) {
        updateUser(connection, username, "role", role, IamError.ROLE_DOES_NOT_EXIST);
    }

    public void updateUserDisplayName(Connection connection, String username, String displayName) {
        updateUser(connection, username, "display-name", displayName, IamError.USER_DOES_NOT_EXIST);
    }

    public void renameUser(Connection connection, String username, String newUsername) {
        updateUser(connection, username, "username", newUsername, IamError.USER_DOES_NOT_EXIST);
    }

    public void deleteUser(Connection connection, String username) {
        String path = "/iam/users/" + username;
        Coap coap = connection.createCoap("DELETE", path);
        int status = execute(coap, connection);
        switch (status) {
            case 202: break;
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            case 404: throw error(IamError.USER_DOES_NOT_EXIST);
            default: throw failed(status);
        }
    }
}
