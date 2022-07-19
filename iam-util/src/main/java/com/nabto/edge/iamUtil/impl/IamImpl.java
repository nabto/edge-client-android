package com.nabto.edge.iamutil.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.lang.Runnable;
import java.lang.IllegalArgumentException;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

import com.nabto.edge.iamutil.*;
import com.nabto.edge.client.Coap.ContentFormat;
import com.nabto.edge.client.Coap;
import com.nabto.edge.client.NabtoRuntimeException;
import com.nabto.edge.client.ErrorCodes;
import com.nabto.edge.client.Connection;
import com.nabto.edge.client.NabtoCallback;

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

    private void executeAsync(Coap coap, Connection connection, boolean probePairing, IamCallback cb, Runnable code) {
        NabtoCallback<Void> coapCallback = (errorCode, arg) -> {
            if (errorCode != ErrorCodes.OK) {
                cb.run(IamError.FAILED, Optional.empty());
                return;
            }

            int status = coap.getResponseStatusCode();

            if (status == 404 && probePairing) {
                // Probe /iam/pairing to see if the device supports IAM
                // @TODO: Unsure if making this a callback is a good idea... further investigation needed
                Coap probe = connection.createCoap("GET", "/iam/pairing");
                NabtoCallback probeCallback = (probeCode, probeArg) -> {
                    int probeStatus = probe.getResponseStatusCode();
                    if (probeStatus != 205 && probeStatus != 403) {
                        // Iam is not supported by the device
                        cb.run(IamError.IAM_NOT_SUPPORTED, Optional.empty());
                    } else {
                        // Iam is supported by the device, but the coap command that was executed was invalid
                        // Let the caller handle it
                        code.run();
                    }
                };
                probe.executeCallback(probeCallback);
            } else {
                code.run();
            }
        };

        coap.executeCallback(coapCallback);
    }

    // @TODO: Deprecate
    private IamException failed(int status) {
        return new IamException(IamError.FAILED, "got unexpected status code " + status);
    }

    // @TODO: Deprecate
    private IamException error(IamError error) {
        return new IamException(error);
    }

    private void maybeThrow(IamError err, int status) {
        if (err == IamError.FAILED) {
            throw failed(status);
        } else if (err != IamError.NONE) {
            throw error(err);
        }
    }

    private enum IamPath {
        PAIR_LOCAL_OPEN,
        PAIR_LOCAL_INITIAL,
        PAIR_PASSWORD_OPEN,
        PAIR_PASSWORD_INVITE,
        GET_DEVICE_DETAILS,
        GET_ME,
        GET_USER
    }

    private Coap createCoap(Connection connection, IamPath path, String... strings) {
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

            default: throw new IllegalArgumentException("Enum switch is not exhaustive");
        }
    }

    private IamError pairLocalOpenInternal(int status) {
        switch (status) {
            case 201: return IamError.NONE;
            case 400: return IamError.INVALID_INPUT;
            case 403: return IamError.BLOCKED_BY_DEVICE_CONFIGURATION;
            case 404: return IamError.PAIRING_MODE_DISABLED;
            case 409: return IamError.USERNAME_EXISTS;
            default: return IamError.FAILED;
        }
    }

    public void pairLocalOpen(Connection connection, String desiredUsername) {
        Coap coap = createCoap(connection, IamPath.PAIR_LOCAL_OPEN);
        IamUser user = new IamUser(desiredUsername);
        coap.setRequestPayload(ContentFormat.APPLICATION_CBOR, encode(user));
        int status = execute(coap, connection);
        IamError err = pairLocalOpenInternal(status);
        maybeThrow(err, status);
    }

    public void pairLocalOpenCallback(Connection connection, String desiredUsername, IamCallback cb) {
        Coap coap = createCoap(connection, IamPath.PAIR_LOCAL_OPEN);
        IamUser user = new IamUser(desiredUsername);
        coap.setRequestPayload(ContentFormat.APPLICATION_CBOR, encode(user));
        executeAsync(coap, connection, true, cb, () -> {
            int status = coap.getResponseStatusCode();
            IamError err = pairLocalOpenInternal(status);
            cb.run(err, Optional.empty());
        });
    }

    private IamError pairLocalInitialInternal(int status) {
        switch (status) {
            case 201: return IamError.NONE;
            case 403: return IamError.BLOCKED_BY_DEVICE_CONFIGURATION;
            case 404: return IamError.PAIRING_MODE_DISABLED;
            case 409: return IamError.INITIAL_USER_ALREADY_PAIRED;
            default: return IamError.FAILED;
        }
    }

    public void pairLocalInitial(Connection connection) {
        Coap coap = createCoap(connection, IamPath.PAIR_LOCAL_INITIAL);
        int status = execute(coap, connection);
        IamError err = pairLocalInitialInternal(status);
        maybeThrow(err, status);
    }

    public void pairLocalInitialCallback(Connection connection, IamCallback cb) {
        Coap coap = createCoap(connection, IamPath.PAIR_LOCAL_INITIAL);
        executeAsync(coap, connection, true, cb, () -> {
            int status = coap.getResponseStatusCode();
            IamError err = pairLocalOpenInternal(status);
            cb.run(err, Optional.empty());
        });
    }

    // @TODO: Callback versions of these
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
        Coap coap = createCoap(connection, IamPath.PAIR_PASSWORD_OPEN);
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
        Coap coap = createCoap(connection, IamPath.PAIR_PASSWORD_INVITE);
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

    private IamError getDeviceDetailsInternal(int status) {
        switch (status) {
            case 205: break;
            case 403: return IamError.BLOCKED_BY_DEVICE_CONFIGURATION;
            case 404: return IamError.IAM_NOT_SUPPORTED;
            default: return IamError.FAILED;
        }
        return IamError.NONE;
    }

    public IamDeviceDetails getDeviceDetails(Connection connection) {
        Coap coap = createCoap(connection, IamPath.GET_DEVICE_DETAILS);
        // Don't use helper execute() here because it's redundant.
        coap.execute();
        int status = coap.getResponseStatusCode();
        IamError err = getDeviceDetailsInternal(coap.getResponseStatusCode());
        if (err != IamError.NONE) {
            throw error(err);
        }
        IamDeviceDetails details = decode(coap.getResponsePayload(), IamDeviceDetails.class);
        return details;
    }

    public void getDeviceDetailsCallback(Connection connection, IamCallback<IamDeviceDetails> cb) {
        Coap coap = createCoap(connection, IamPath.GET_DEVICE_DETAILS);
        executeAsync(coap, connection, false, cb, () -> {
            IamError err = getDeviceDetailsInternal(coap.getResponseStatusCode());
            if (err != IamError.NONE) {
                cb.run(err, Optional.empty());
                return;
            }
            IamDeviceDetails details = decode(coap.getResponsePayload(), IamDeviceDetails.class);
            cb.run(IamError.NONE, Optional.of(details));
        });
    }

    public boolean isCurrentUserPaired(Connection connection) {
        Coap coap = createCoap(connection, IamPath.GET_ME);
        int status = execute(coap, connection);
        switch (status) {
            case 205: return true;
            case 403: throw error(IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            case 404: return false;
            default: throw failed(status);
        }
    }

    public void isCurrentUserPairedCallback(Connection connection, IamCallback<Boolean> cb) {
        Coap coap = createCoap(connection, IamPath.GET_ME);
        executeAsync(coap, connection, true, cb, () -> {
            int status = coap.getResponseStatusCode();
            boolean ret = false;
            switch (status) {
                case 205: cb.run(IamError.NONE, Optional.of(true)); break;
                case 403: cb.run(IamError.BLOCKED_BY_DEVICE_CONFIGURATION, Optional.empty()); break;
                case 404: cb.run(IamError.NONE, Optional.of(false)); break;
                default: cb.run(IamError.FAILED, Optional.empty());
            }
        });
    }

    private IamError getUserInternal(int status) {
        switch (status) {
            case 205: return IamError.NONE;
            case 403: return IamError.BLOCKED_BY_DEVICE_CONFIGURATION;
            case 404: return IamError.USER_DOES_NOT_EXIST;
            default: return IamError.FAILED;
        }
    }

    public IamUser getUser(Connection connection, String username) {
        Coap coap = createCoap(connection, IamPath.GET_USER, username);
        int status = execute(coap, connection);
        IamError err = getUserInternal(status);
        maybeThrow(err, status);
        IamUser user = decode(coap.getResponsePayload(), IamUser.class);
        return user;
    }
/*
    {
        IamUser user = composer
            .start(IamInvoke.GET_USER, username)
            .withMap(map)
            .execute()
            .maybeThrow()
            .decodePayload(IamUser.class);
        return user;
    }

    {
        composer
            .startAsync(IamInvoke.GET_USER, username)
            .setUserCallback(cb)
            .withMap(map)
            .execute(connection);
    }
*/

    public void getUserCallback(Connection connection, String username, IamCallback<IamUser> cb) {
        Coap coap = createCoap(connection, IamPath.GET_USER, username);
        executeAsync(coap, connection, true, cb, () -> {
            int status = coap.getResponseStatusCode();
            IamError err = getUserInternal(status);
            if (err == IamError.NONE) {
                IamUser user = decode(coap.getResponsePayload(), IamUser.class);
                cb.run(err, Optional.of(user));
            } else {
                cb.run(err, Optional.empty());
            }
        });
    }

    private IamError getCurrentUserInternal(int status) {
        switch (status) {
            case 205: return IamError.NONE;
            case 403: return IamError.BLOCKED_BY_DEVICE_CONFIGURATION;
            case 404: return IamError.USER_DOES_NOT_EXIST;
            default: return IamError.FAILED;
        }
    }

    public IamUser getCurrentUser(Connection connection) {
        Coap coap = connection.createCoap("GET", "/iam/me");
        int status = execute(coap, connection);
        IamError err = getCurrentUserInternal(status);
        maybeThrow(err, status);
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
