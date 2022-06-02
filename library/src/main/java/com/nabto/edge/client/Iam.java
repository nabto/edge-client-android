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

    public static Iam create(Connection connection) {
        return new IamImpl(connection);
    }

    public abstract void pairLocalOpen(String desiredUsername);

    public abstract void pairLocalInitial();

    public abstract void pairPasswordOpen(String desiredUsername, String password);

    public abstract void pairPasswordInvite(String username, String password);

    public abstract PairingMode[] getAvailablePairingModes();

    public abstract IamDeviceDetails getDeviceDetails();

    public abstract boolean isCurrentUserPaired();

    public abstract IamUser getUser(String username);
    
    public abstract IamUser getCurrentUser();

    public abstract void createUser(String username, String password, String role);

    public abstract void updateUserPassword(String username, String password);
    
    public abstract void updateUserRole(String username, String role);

    public abstract void updateUserDisplayName(String username, String displayName);

    public abstract void renameUser(String username, String newUsername);

    public abstract void deleteUser(String username);
}
