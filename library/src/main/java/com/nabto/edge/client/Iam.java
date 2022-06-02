package com.nabto.edge.client;

import com.nabto.edge.client.impl.IamImpl;

// @TODO: Documentation

public abstract class Iam {
    public enum PairingMode {
        LOCAL_OPEN,
        LOCAL_INITIAL,
        PASSWORD_OPEN,
        PASSWORD_INVITE
    }

    public static Iam create() {
        return new IamImpl();
    }

    public abstract void pairLocalOpen(Connection connection, String desiredUsername);

    public abstract void pairLocalInitial(Connection connection);

    public abstract void pairPasswordOpen(Connection connection, String desiredUsername, String password);

    public abstract void pairPasswordInvite(Connection connection, String username, String password);

    public abstract PairingMode[] getAvailablePairingModes(Connection connection);

    public abstract IamDeviceDetails getDeviceDetails(Connection connection);

    public abstract boolean isCurrentUserPaired(Connection connection);

    public abstract IamUser getUser(Connection connection, String username);
    
    public abstract IamUser getCurrentUser(Connection connection);

    public abstract void createUser(Connection connection, String username, String password, String role);

    public abstract void updateUserPassword(Connection connection, String username, String password);
    
    public abstract void updateUserRole(Connection connection, String username, String role);

    public abstract void updateUserDisplayName(Connection connection, String username, String displayName);

    public abstract void renameUser(Connection connection, String username, String newUsername);

    public abstract void deleteUser(Connection connection, String username);
}
