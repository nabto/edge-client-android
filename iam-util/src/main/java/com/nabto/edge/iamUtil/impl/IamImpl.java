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
    public void pairLocalOpen(Connection connection, String desiredUsername) {
        IamComposer composer = new IamComposer();
        composer
            .start(connection, IamPath.PAIR_LOCAL_OPEN)
            .withPayload(new IamUser(desiredUsername))
            .withMap(new Object[][] {
                {201, IamError.NONE},
                {400, IamError.INVALID_INPUT},
                {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                {404, IamError.PAIRING_MODE_DISABLED},
                {409, IamError.USERNAME_EXISTS}
            })
            .execute()
            .maybeThrow();
    }

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

    public void pairLocalInitial(Connection connection) {
        IamComposer composer = new IamComposer();
        composer
            .start(connection, IamPath.PAIR_LOCAL_INITIAL)
            .withMap(new Object[][] {
                {201, IamError.NONE},
                {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                {404, IamError.PAIRING_MODE_DISABLED},
                {409, IamError.INITIAL_USER_ALREADY_PAIRED}
            })
            .execute()
            .maybeThrow();
    }

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

    public void pairPasswordOpen(Connection connection, String desiredUsername, String password) {
        passwordAuthenticate(connection, "", password);
        IamComposer composer = new IamComposer();
        composer
            .start(connection, IamPath.PAIR_PASSWORD_OPEN)
            .withPayload(new IamUser(desiredUsername))
            .withMap(new Object[][] {
                {201, IamError.NONE},
                {400, IamError.INVALID_INPUT},
                {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                {404, IamError.PAIRING_MODE_DISABLED},
                {409, IamError.USERNAME_EXISTS}
            })
            .execute()
            .maybeThrow();
    }

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

    public void pairPasswordInvite(Connection connection, String username, String password) {
        passwordAuthenticate(connection, username, password);
        IamComposer composer = new IamComposer();
        composer
            .start(connection, IamPath.PAIR_PASSWORD_INVITE)
            .withMap(new Object[][] {
                {201, IamError.NONE},
                {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                {404, IamError.PAIRING_MODE_DISABLED}
            })
            .execute()
            .maybeThrow();
    }

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
                default: throw new IamException(IamError.FAILED, "unknown mode " + stringMode);
            }
            list.add(mode);
        }
        return list.toArray(new PairingMode[list.size()]);
    }

    public DeviceDetails getDeviceDetails(Connection connection) {
        IamComposer composer = new IamComposer();
        DeviceDetails details = composer
            .start(connection, IamPath.GET_DEVICE_DETAILS)
            .withMap(new Object[][] {
                {205, IamError.NONE},
                {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                {404, IamError.IAM_NOT_SUPPORTED}
            })
            .execute(false)
            .maybeThrow()
            .decodePayload(DeviceDetails.class);
        return details;
    }

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

    public boolean isCurrentUserPaired(Connection connection) {
        IamComposer composer = new IamComposer();
        int status = composer
            .start(connection, IamPath.GET_ME)
            .withMap(new Object[][] {
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

    public IamUser getUser(Connection connection, String username) {
        IamComposer composer = new IamComposer();
        IamUser user = composer
            .start(connection, IamPath.GET_USER, username)
            .withMap(new Object[][] {
                {205, IamError.NONE},
                {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                {404, IamError.USER_DOES_NOT_EXIST}
            })
            .execute()
            .maybeThrow()
            .decodePayload(IamUser.class);
        return user;
    }

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

    public IamUser getCurrentUser(Connection connection) {
        IamComposer composer = new IamComposer();
        IamUser user = composer
            .start(connection, IamPath.GET_ME)
            .withMap(new Object[][] {
                {205, IamError.NONE},
                {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                {404, IamError.USER_DOES_NOT_EXIST}
            })
            .execute()
            .maybeThrow()
            .decodePayload(IamUser.class);
        return user;
    }

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

    private void updateUser(Connection connection, String username, String key, String value, IamError error404) {
        // @TODO: 404 has different semantics depending on what went wrong
        //        E.g. it could mean the user doesnt exist, the role doesnt exist...
        //        We have two errors USER_DOES_NOT_EXIST and ROLE_DOES_NOT_EXIST
        //        and we can only report one. Shouldn't they just be one error?
        //        such as INVALID_USER_UPDATE_SETTINGS or something.
        IamComposer composer = new IamComposer();
        composer
            .start(connection, IamPath.UPDATE_USER, username, key)
            .withPayload(value)
            .withMap(new Object[][] {
                {204, IamError.NONE},
                {400, IamError.INVALID_INPUT},
                {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                {404, error404}
            })
            .execute()
            .maybeThrow();
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
                {404, error404}
            })
            .execute();
    }

    public void createUser(Connection connection, String username, String password, String role) {
        IamComposer composer = new IamComposer();
        composer
            .start(connection, IamPath.CREATE_USER)
            .withPayload(new IamUser(username))
            .withMap(new Object[][] {
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

    public void updateUserPassword(Connection connection, String username, String password) {
        updateUser(connection, username, "password", password, IamError.USER_DOES_NOT_EXIST);
    }

    public void updateUserPasswordCallback(Connection connection, String username, String password, IamCallback cb) {
        updateUserCallback(connection, username, "password", password, IamError.USER_DOES_NOT_EXIST, cb);
    }

    public void updateUserRole(Connection connection, String username, String role) {
        updateUser(connection, username, "role", role, IamError.ROLE_DOES_NOT_EXIST);
    }

    public void updateUserRoleCallback(Connection connection, String username, String role, IamCallback cb) {
        updateUserCallback(connection, username, "role", role, IamError.ROLE_DOES_NOT_EXIST, cb);
    }

    public void updateUserDisplayName(Connection connection, String username, String displayName) {
        updateUser(connection, username, "display-name", displayName, IamError.USER_DOES_NOT_EXIST);
    }

    public void updateUserDisplayNameCallback(Connection connection, String username, String displayName, IamCallback cb) {
        updateUserCallback(connection, username, "display-name", displayName, IamError.USER_DOES_NOT_EXIST, cb);
    }

    public void renameUser(Connection connection, String username, String newUsername) {
        updateUser(connection, username, "username", newUsername, IamError.USER_DOES_NOT_EXIST);
    }

    public void renameUserCallback(Connection connection, String username, String newUsername, IamCallback cb) {
        updateUserCallback(connection, username, "username", newUsername, IamError.USER_DOES_NOT_EXIST, cb);
    }

    public void deleteUser(Connection connection, String username) {
        IamComposer composer = new IamComposer();
        composer
            .start(connection, IamPath.DELETE_USER, username)
            .withMap(new Object[][] {
                {202, IamError.NONE},
                {403, IamError.BLOCKED_BY_DEVICE_CONFIGURATION},
                {404, IamError.USER_DOES_NOT_EXIST}
            })
            .execute()
            .maybeThrow();
    }

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
