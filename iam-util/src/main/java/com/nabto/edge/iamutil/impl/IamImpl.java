package com.nabto.edge.iamutil.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.lang.Runnable;
import java.lang.IllegalArgumentException;

import com.nabto.edge.iamutil.*;
import com.nabto.edge.client.*;
import com.nabto.edge.client.Coap.ContentFormat;

public class IamImpl extends IamUtil {
    @Override
    public void pairLocalOpen(Connection connection, String desiredUsername) {
        try (IamComposer composer = new IamComposer()) {
            composer
                    .start(connection, IamPath.PAIR_LOCAL_OPEN)
                    .withPayload(new IamUser(desiredUsername))
                    .withMap(new Object[][]{
                            {201, IamError.NONE},
                            {400, IamError.INVALID_INPUT},
                            {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                            {404, IamError.PAIRING_MODE_DISABLED},
                            {409, IamError.USERNAME_EXISTS}
                    })
                    .execute()
                    .maybeThrow();
        }
    }

    @Override
    public void pairLocalOpenCallback(Connection connection, String desiredUsername, IamCallback cb) {
        IamComposerAsync composer = new IamComposerAsync();
        composer
            .start(connection, IamPath.PAIR_LOCAL_OPEN)
            .withPayload(new IamUser(desiredUsername))
            .withUserCallback(cb)
            .withMap(new Object[][] {
                {201, IamError.NONE},
                {400, IamError.INVALID_INPUT},
                {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                {404, IamError.PAIRING_MODE_DISABLED},
                {409, IamError.USERNAME_EXISTS}
            })
            .execute();
    }

    @Override
    public void pairLocalInitial(Connection connection) {
        try (IamComposer composer = new IamComposer()) {
            composer
                    .start(connection, IamPath.PAIR_LOCAL_INITIAL)
                    .withMap(new Object[][]{
                            {201, IamError.NONE},
                            {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                            {404, IamError.PAIRING_MODE_DISABLED},
                            {409, IamError.INITIAL_USER_ALREADY_PAIRED}
                    })
                    .execute()
                    .maybeThrow();
        }
    }

    @Override
    public void pairLocalInitialCallback(Connection connection, IamCallback cb) {
        IamComposerAsync composer = new IamComposerAsync();
        composer
            .start(connection, IamPath.PAIR_LOCAL_INITIAL)
            .withUserCallback(cb)
            .withMap(new Object[][] {
                {201, IamError.NONE},
                {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                {404, IamError.PAIRING_MODE_DISABLED},
                {409, IamError.INITIAL_USER_ALREADY_PAIRED}
            })
            .execute();
    }

    private void passwordAuthenticate(Connection connection, String username, String password) {
        try {
            connection.passwordAuthenticate(username, password);
        } catch (NabtoRuntimeException e) {
            if (e.getErrorCode().getErrorCode() == ErrorCodes.UNAUTHORIZED) {
                throw new IamException(IamError.AUTHENTICATION_ERROR);
            } else {
                throw e;
            }
        }
    }

    @Override
    public void pairPasswordOpen(Connection connection, String desiredUsername, String password) {
        passwordAuthenticate(connection, "", password);
        try (IamComposer composer = new IamComposer()) {
            composer
                    .start(connection, IamPath.PAIR_PASSWORD_OPEN)
                    .withPayload(new IamUser(desiredUsername))
                    .withMap(new Object[][]{
                            {201, IamError.NONE},
                            {400, IamError.INVALID_INPUT},
                            {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                            {404, IamError.PAIRING_MODE_DISABLED},
                            {409, IamError.USERNAME_EXISTS}
                    })
                    .execute()
                    .maybeThrow();
        }
    }

    @Override
    public void pairPasswordOpenCallback(Connection connection, String desiredUsername, String password, IamCallback cb) {
        connection.passwordAuthenticateCallback("", password, (ec, arg) -> {
            if (ec != ErrorCodes.OK) {
                if (ec == ErrorCodes.UNAUTHORIZED) {
                    cb.run(IamError.AUTHENTICATION_ERROR, Optional.empty());
                } else {
                    cb.run(IamError.FAILED, Optional.empty());
                }
                return;
            }
            IamComposerAsync composer = new IamComposerAsync();
            composer
                .start(connection, IamPath.PAIR_PASSWORD_OPEN)
                .withUserCallback(cb)
                .withPayload(new IamUser(desiredUsername))
                .withMap(new Object[][] {
                    {201, IamError.NONE},
                    {400, IamError.INVALID_INPUT},
                    {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                    {404, IamError.PAIRING_MODE_DISABLED},
                    {409, IamError.USERNAME_EXISTS}
                })
                .execute();
        });
    }

    @Override
    public void pairPasswordInvite(Connection connection, String username, String password) {
        passwordAuthenticate(connection, username, password);
        try (IamComposer composer = new IamComposer()) {
            composer
                    .start(connection, IamPath.PAIR_PASSWORD_INVITE)
                    .withMap(new Object[][]{
                            {201, IamError.NONE},
                            {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                            {404, IamError.PAIRING_MODE_DISABLED}
                    })
                    .execute()
                    .maybeThrow();
        }
    }

    @Override
    public void pairPasswordInviteCallback(Connection connection, String username, String password, IamCallback cb) {
        connection.passwordAuthenticateCallback(username, password, (ec, arg) -> {
            if (ec != ErrorCodes.OK) {
                if (ec == ErrorCodes.UNAUTHORIZED) {
                    cb.run(IamError.AUTHENTICATION_ERROR, Optional.empty());
                } else {
                    cb.run(IamError.FAILED, Optional.empty());
                }
                return;
            }
            IamComposerAsync composer = new IamComposerAsync();
            composer
                .start(connection, IamPath.PAIR_PASSWORD_INVITE)
                .withUserCallback(cb)
                .withMap(new Object[][] {
                    {201, IamError.NONE},
                    {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                    {404, IamError.PAIRING_MODE_DISABLED}
                })
                .execute();
        });
    }

    @Override
    public PairingMode[] getAvailablePairingModes(Connection connection) {
        DeviceDetails details = getDeviceDetails(connection);
        ArrayList<PairingMode> list = new ArrayList<>();
        for (String stringMode : details.getModes()) {
            PairingMode mode;
            switch (stringMode) {
                case "LocalInitial": mode = PairingMode.LOCAL_INITIAL; break;
                case "LocalOpen": mode = PairingMode.LOCAL_OPEN; break;
                case "PasswordInvite": mode = PairingMode.PASSWORD_INVITE; break;
                case "PasswordOpen": mode = PairingMode.PASSWORD_OPEN; break;
                default: continue;
            }
            list.add(mode);
        }
        return list.toArray(new PairingMode[list.size()]);
    }

    @Override
    public DeviceDetails getDeviceDetails(Connection connection) {
        try (IamComposer composer = new IamComposer()) {
            DeviceDetails details = composer
                    .start(connection, IamPath.GET_DEVICE_DETAILS)
                    .withMap(new Object[][]{
                            {205, IamError.NONE},
                            {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                            {404, IamError.IAM_NOT_SUPPORTED}
                    })
                    .execute(false)
                    .maybeThrow()
                    .decodePayload(DeviceDetails.class);
            return details;
        }
    }

    @Override
    public void getDeviceDetailsCallback(Connection connection, IamCallback<DeviceDetails> cb) {
        IamComposerAsync<DeviceDetails> composer = new IamComposerAsync<>();
        composer
            .start(connection, IamPath.GET_DEVICE_DETAILS)
            .withResultType(DeviceDetails.class)
            .withUserCallback(cb)
            .withMap(new Object[][] {
                {205, IamError.NONE},
                {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                {404, IamError.IAM_NOT_SUPPORTED}
            })
            .execute(false);
    }

    @Override
    public boolean isCurrentUserPaired(Connection connection) {
        try (IamComposer composer = new IamComposer()) {
            int status = composer
                    .start(connection, IamPath.GET_ME)
                    .withMap(new Object[][]{
                            {205, IamError.NONE},
                            {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                            {404, IamError.NONE}
                    })
                    .execute()
                    .maybeThrow()
                    .getStatus();
            if (status == 205) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void isCurrentUserPairedCallback(Connection connection, IamCallback<Boolean> cb) {
        IamComposerAsync<Boolean> composer = new IamComposerAsync<>();
        composer
            .start(connection, IamPath.GET_ME)
            .withUserCallback(cb)
            .withMap(new Object[][] {
                {205, IamError.NONE},
                {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                {404, IamError.NONE}
            })
            .withResultMap(new Object[][] {
                {205, true},
                {404, false}
            })
            .execute();
    }

    @Override
    public IamUser getUser(Connection connection, String username) {
        try (IamComposer composer = new IamComposer()) {
            IamUser user = composer
                    .start(connection, IamPath.GET_USER, username)
                    .withMap(new Object[][]{
                            {205, IamError.NONE},
                            {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                            {404, IamError.USER_DOES_NOT_EXIST}
                    })
                    .execute()
                    .maybeThrow()
                    .decodePayload(IamUser.class);
            return user;
        }
    }

    @Override
    public void getUserCallback(Connection connection, String username, IamCallback<IamUser> cb) {
        IamComposerAsync<IamUser> composer = new IamComposerAsync<>();
        composer
            .start(connection, IamPath.GET_USER, username)
            .withResultType(IamUser.class)
            .withUserCallback(cb)
            .withMap(new Object[][] {
                {205, IamError.NONE},
                {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                {404, IamError.USER_DOES_NOT_EXIST}
            })
            .execute();
    }

    @Override
    public IamUser getCurrentUser(Connection connection) {
        try (IamComposer composer = new IamComposer()) {
            IamUser user = composer
                    .start(connection, IamPath.GET_ME)
                    .withMap(new Object[][]{
                            {205, IamError.NONE},
                            {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                            {404, IamError.USER_DOES_NOT_EXIST}
                    })
                    .execute()
                    .maybeThrow()
                    .decodePayload(IamUser.class);
            return user;
        }
    }

    @Override
    public void getCurrentUserCallback(Connection connection, IamCallback<IamUser> cb) {
        IamComposerAsync<IamUser> composer = new IamComposerAsync<>();
        composer
            .start(connection, IamPath.GET_ME)
            .withResultType(IamUser.class)
            .withUserCallback(cb)
            .withMap(new Object[][] {
                {205, IamError.NONE},
                {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                {404, IamError.USER_DOES_NOT_EXIST}
            })
            .execute();
    }

    @Override
    public String[] getAvailableRoles(Connection connection) {
        try (IamComposer composer = new IamComposer()) {
            String[] result = composer
                    .start(connection, IamPath.GET_ROLES)
                    .withMap(new Object[][]{
                            {205, IamError.NONE},
                            {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION}
                    })
                    .execute()
                    .maybeThrow()
                    .decodePayload(String[].class);
            return result;
        }
    }

    @Override
    public void getAvailableRolesCallback(Connection connection, IamCallback<String[]> cb) {
        IamComposerAsync<String[]> composer = new IamComposerAsync<>();
        composer
            .start(connection, IamPath.GET_ROLES)
            .withResultType(String[].class)
            .withUserCallback(cb)
            .withMap(new Object[][] {
                {205, IamError.NONE},
                {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION}
            })
            .execute();
    }

    private void updateUser(Connection connection, String username, String key, String value, IamError error404) {
        // @TODO: 404 has different semantics depending on what went wrong
        //        E.g. it could mean the user doesnt exist, the role doesnt exist...
        //        We have two errors USER_DOES_NOT_EXIST and ROLE_DOES_NOT_EXIST
        //        and we can only report one. Shouldn't they just be one error?
        //        such as INVALID_USER_UPDATE_SETTINGS or something.
        try (IamComposer composer = new IamComposer()) {
            composer
                    .start(connection, IamPath.UPDATE_USER, username, key)
                    .withPayload(value)
                    .withMap(new Object[][]{
                            {204, IamError.NONE},
                            {400, IamError.INVALID_INPUT},
                            {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                            {404, error404}
                    })
                    .execute()
                    .maybeThrow();
        }
    }

    private void updateUserCallback(Connection connection, String username, String key, String value, IamError error404, IamCallback cb) {
        IamComposerAsync composer = new IamComposerAsync();
        composer
            .start(connection, IamPath.UPDATE_USER, username, key)
            .withUserCallback(cb)
            .withPayload(value)
            .withMap(new Object[][] {
                {204, IamError.NONE},
                {400, IamError.INVALID_INPUT},
                {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                {409, IamError.USERNAME_EXISTS},
                {404, error404}
            })
            .execute();
    }

    @Override
    public void createUser(Connection connection, String username, String password, String role) {
        try (IamComposer composer = new IamComposer()) {
            composer
                    .start(connection, IamPath.CREATE_USER)
                    .withPayload(new IamUser(username))
                    .withMap(new Object[][]{
                            {201, IamError.NONE},
                            {400, IamError.INVALID_INPUT},
                            {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                            {409, IamError.USERNAME_EXISTS}
                    })
                    .execute()
                    .maybeThrow();
            updateUser(connection, username, "password", password, IamError.USER_DOES_NOT_EXIST);
            updateUser(connection, username, "role", role, IamError.ROLE_DOES_NOT_EXIST);
        }
    }

    @Override
    public void createUserCallback(Connection connection, String username, String password, String role, IamCallback cb)
    {
        IamComposerAsync composer = new IamComposerAsync();
        composer
            .start(connection, IamPath.CREATE_USER)
            .withUserCallback(cb)
            .withPayload(new IamUser(username))
            .withMap(new Object[][] {
                {201, IamError.NONE},
                {400, IamError.INVALID_INPUT},
                {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                {409, IamError.USERNAME_EXISTS}
            })
            .then(connection, IamPath.UPDATE_USER, username, "password")
            .withPayload(password)
            .withMap(new Object[][] {
                {204, IamError.NONE},
                {400, IamError.INVALID_INPUT},
                {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                {404, IamError.USER_DOES_NOT_EXIST}
            })
            .then(connection, IamPath.UPDATE_USER, username, "role")
            .withPayload(role)
            .withMap(new Object[][] {
                {204, IamError.NONE},
                {400, IamError.INVALID_INPUT},
                {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                {404, IamError.ROLE_DOES_NOT_EXIST}
            })
            .execute();
    }

    @Override
    public void updateUserPassword(Connection connection, String username, String password) {
        updateUser(connection, username, "password", password, IamError.USER_DOES_NOT_EXIST);
    }

    @Override
    public void updateUserPasswordCallback(Connection connection, String username, String password, IamCallback cb) {
        updateUserCallback(connection, username, "password", password, IamError.USER_DOES_NOT_EXIST, cb);
    }

    @Override
    public void updateUserRole(Connection connection, String username, String role) {
        updateUser(connection, username, "role", role, IamError.ROLE_DOES_NOT_EXIST);
    }

    @Override
    public void updateUserRoleCallback(Connection connection, String username, String role, IamCallback cb) {
        updateUserCallback(connection, username, "role", role, IamError.ROLE_DOES_NOT_EXIST, cb);
    }

    @Override
    public void updateUserDisplayName(Connection connection, String username, String displayName) {
        updateUser(connection, username, "display-name", displayName, IamError.USER_DOES_NOT_EXIST);
    }

    @Override
    public void updateUserDisplayNameCallback(Connection connection, String username, String displayName, IamCallback cb) {
        updateUserCallback(connection, username, "display-name", displayName, IamError.USER_DOES_NOT_EXIST, cb);
    }

    @Override
    public void renameUser(Connection connection, String username, String newUsername) {
        updateUser(connection, username, "username", newUsername, IamError.USER_DOES_NOT_EXIST);
    }

    @Override
    public void renameUserCallback(Connection connection, String username, String newUsername, IamCallback cb) {
        updateUserCallback(connection, username, "username", newUsername, IamError.USER_DOES_NOT_EXIST, cb);
    }

    @Override
    public void deleteUser(Connection connection, String username) {
        try (IamComposer composer = new IamComposer()) {
            composer
                    .start(connection, IamPath.DELETE_USER, username)
                    .withMap(new Object[][]{
                            {202, IamError.NONE},
                            {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                            {404, IamError.USER_DOES_NOT_EXIST}
                    })
                    .execute()
                    .maybeThrow();
        }
    }

    @Override
    public void deleteUserCallback(Connection connection, String username, IamCallback cb) {
        IamComposerAsync composer = new IamComposerAsync();
        composer
            .start(connection, IamPath.DELETE_USER, username)
            .withUserCallback(cb)
            .withMap(new Object[][] {
                {202, IamError.NONE},
                {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                {404, IamError.USER_DOES_NOT_EXIST}
            })
            .execute();
    }
}
