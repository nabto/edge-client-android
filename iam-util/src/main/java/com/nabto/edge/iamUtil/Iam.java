package com.nabto.edge.iamutil;

import com.nabto.edge.iamutil.impl.IamImpl;
import com.nabto.edge.client.Connection;

/**
 * This class simplifies interaction with the Nabto Edge Embedded SDK device's CoAP IAM endpoints.
 *
 * For instance, it is made simple to invoke the different pairing endpoints - just invoke a simple high level
 * pairing function to pair the client with the connected device and don't worry about CBOR encoding and decoding.
 *
 * Read more about the important concept of pairing here: https://docs.nabto.com/developer/guides/concepts/iam/pairing.html
 *
 * All the most popular IAM device endpoints are wrapped to also allow management of the user profile on the device
 * (own or other users' if client is in admin role).
 *
 * Note that the device's IAM configuration must allow invocation of the different functions and the pairing modes must
 * be enabled at runtime. Read more about that in the general IAM intro here: https://docs.nabto.com/developer/guides/concepts/iam/intro.html
 */
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

    /**
     * Perform Local Open pairing, requesting the specified username.
     *
     * Local open pairing uses the trusted local network (LAN) pairing mechanism. No password is required for pairing and no
     * invitation is needed, anybody on the LAN can initiate pairing.
     *
     * Read more here: https://docs.nabto.com/developer/guides/concepts/iam/pairing.html#open-local
     *
     * @param connection An established connection to the device this client should be paired with
     * @param desiredUsername Assign this username on the device if available (pairing fails with .USERNAME_EXISTS if not)
     *
     * @throws USERNAME_EXISTS if desiredUsername is already in use on the device
     * @throws INVALID_INPUT if desiredUsername is not valid as per https://docs.nabto.com/developer/api-reference/coap/iam/post-users.html#request
     * @throws BLOCKED_BY_DEVICE_CONFIGURATION if the device configuration does not support local open pairing (the `IAM:PairingLocalOpen` action
     * is not set for the Unpaired role or the device does not support the pairing mode at all)
     * @throws PAIRING_MODE_DISABLED if the pairing mode is configured on the device but is disabled at runtime
     * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
     */
    public abstract void pairLocalOpen(Connection connection, String desiredUsername);

    /**
     * Perform Local Initial pairing, assigning the default initial username configured on the device (typically "admin").
     *
     * In this mode, the initial user can be paired on the local network without providing a username or password - and
     * only the initial user. This is a typical bootstrap scenario to pair the admin user (device owner).
     *
     * Read more here: https://docs.nabto.com/developer/guides/concepts/iam/pairing.html#initial-local
     *
     * @param connection An established connection to the device this client should be paired with
     *
     * @throws INITIAL_USER_ALREADY_PAIRED if the initial user was already paired
     * @throws BLOCKED_BY_DEVICE_CONFIGURATION if the device configuration does not support local open pairing (the `IAM:PairingLocalInitial` action
     * is not set for the Unpaired role or the device does not support the pairing mode at all)
     * @throws PAIRING_MODE_DISABLED if the pairing mode is configured on the device but is disabled at runtime.
     * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
     */
    public abstract void pairLocalInitial(Connection connection);

    /**
     * Perform Password Open pairing, requesting the specified username and authenticating using the specified password.
     *
     * In this mode a device has set a password which can be used in the pairing process to grant a client access to the
     * device. The client can pair remotely to the device if necessary; it is not necessary to be on the same LAN.
     *
     * Read more here: https://docs.nabto.com/developer/guides/concepts/iam/pairing.html#open-password
     *
     * @param connection An established connection to the device this client should be paired with
     * @param desiredUsername Assign this username on the device if available (pairing fails with .USERNAME_EXISTS if not)
     * @param password the common (not user-specific) password to allow pairing using Password Open pairing
     *
     * @throws USERNAME_EXISTS if desiredUsername is already in use on the device
     * @throws AUTHENTICATION_ERROR if the open pairing password was invalid for the device
     * @throws INVALID_INPUT if desiredUsername is not valid as per https://docs.nabto.com/developer/api-reference/coap/iam/post-users.html#request
     * @throws INITIAL_USER_ALREADY_PAIRED if the initial user was already paired
     * @throws BLOCKED_BY_DEVICE_CONFIGURATION if the device configuration does not support local open pairing (the `IAM:PairingPasswordOpen` action
     * is not set for the Unpaired role or the device does not support the pairing mode at all)
     * @throws PAIRING_MODE_DISABLED if the pairing mode is configured on the device but is disabled at runtime
     * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
     */
    public abstract void pairPasswordOpen(Connection connection, String desiredUsername, String password);

    /**
     * Perform Password Invite pairing, authenticating with the specified username and password.
     *
     * In the Password invite pairing mode a user is required in the system to be able to pair: An existing user (or
     * the system autonomously) creates a username and password that is somehow passed to the new user (an invitation).
     *
     * Read more here: https://docs.nabto.com/developer/guides/concepts/iam/pairing.html#invite
     *
     * @param connection An established connection to the device this client should be paired with
     * @param username Username for the invited user
     * @param password Password for the invited user
     *
     * @throws AUTHENTICATION_ERROR if authentication failed using the specified username/password combination for the device
     * @throws BLOCKED_BY_DEVICE_CONFIGURATION if the device configuration does not support local open pairing (the `IAM:PairingPasswordInvite` action
     * is not set for the Unpaired role or the device does not support the pairing mode at all)
     * @throws PAIRING_MODE_DISABLED if the pairing mode is configured on the device but is disabled at runtime
     * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
     */
    public abstract void pairPasswordInvite(Connection connection, String username, String password);

    /**
     * Retrieve a list of the available pairing modes on the device.
     *
     * @param connection An established connection to the device
     *
     * @throws BLOCKED_BY_DEVICE_CONFIGURATION if the device configuration does not allow retrieving this list (the
     * `IAM:GetPairing` action is not set for the Unpaired role)
     * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
     */
    public abstract PairingMode[] getAvailablePairingModes(Connection connection);

    /**
     * Retrieve device information that typically does not need a paired user.
     *
     * @param connection An established connection to the device
     *
     * @throws BLOCKED_BY_DEVICE_CONFIGURATION if the device configuration does not allow retrieving this list (the
     * `IAM:GetPairing` action is not set for the Unpaired role)
     * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
     */
    public abstract IamDeviceDetails getDeviceDetails(Connection connection);

    /**
     * Query if the current user is paired or not on a specific device.
     *
     * @param connection An established connection to the device
     * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
     * @return true iff the current user is paired with the device
     */
    public abstract boolean isCurrentUserPaired(Connection connection);

    /**
     * Get details about a specific user.
     *
     * @param connection An established connection to the device
     *
     * @throws USER_DOES_NOT_EXIST if the user does not exist on the device
     * @throws BLOCKED_BY_DEVICE_CONFIGURATION if the device configuration does not allow retrieving this user  (the
     * `IAM:GetUser` action is not set for the requesting role)
     * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
     * @return an IamUser instance describing the requested user
     */
    public abstract IamUser getUser(Connection connection, String username);

    /**
     * Get details about the user that has opened the current connection to the device.
     *
     * @param connection An established connection to the device
     *
     * @throws USER_DOES_NOT_EXIST if the current user is not paired with the device.
     * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
     * @return an IamUser instance describing the current user
     */
    public abstract IamUser getCurrentUser(Connection connection);

    /**
     * Create an IAM user on device.
     *
     * See https://docs.nabto.com/developer/guides/concepts/iam/intro.html for an intro to the concept of users and roles.
     *
     * @param connection An established connection to the device
     * @param username Username for the new user
     * @param password Password for the new user
     * @param role IAM role for the new user
     * @throws INVALID_INPUT if username is not valid as per https://docs.nabto.com/developer/api-reference/coap/iam/post-users.html#request
     * @throws BLOCKED_BY_DEVICE_CONFIGURATION if the device configuration does not allow the current user to create a new user (the
     * `IAM:CreateUser` action is not allowed for the requesting role)
     * @throws ROLE_DOES_NOT_EXIST the specified role does not exist in the device IAM configuration
     * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
     */
    public abstract void createUser(Connection connection, String username, String password, String role);

    /**
     * Update an IAM user's password on device.
     *
     * @param connection An established connection to the device
     * @param username Username for the user that should have password updated
     * @param password New password for the user
     * @throws BLOCKED_BY_DEVICE_CONFIGURATION if the device configuration does not allow the current user to update the specified user's password (the
     * `IAM:SetUserPassword` action is not allowed for the requesting role for the `IAM:Username` user)
     * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
     */
    public abstract void updateUserPassword(Connection connection, String username, String password);

    /**
     * Update an IAM user's role on device.
     *
     * Known issue: This function currently assumes the user exists. To be able to interpret the
     * ROLE_DOES_NOT_EXIST code correctly, this assumption most hold. Later it can gracefully handle
     * non-existing users
     *
     * See https://docs.nabto.com/developer/guides/concepts/iam/intro.html for an intro to the concept of roles.
     *
     * @param connection An established connection to the device
     * @param username Username for the user that should have password updated
     * @param role New role for the user
     * @throws USER_DOES_NOT_EXIST if the specified user does not exist on the device (see note above)
     * @throws ROLE_DOES_NOT_EXIST the specified role does not exist in the device IAM configuration (see note above)
     * @throws BLOCKED_BY_DEVICE_CONFIGURATION if the device configuration does not allow the current user to update the specified user's role (the
     * `IAM:SetUserRole` action is not allowed for the requesting role for the `IAM:Username` user)
     * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
     */
    public abstract void updateUserRole(Connection connection, String username, String role);

    /**
     * Update an IAM user's display name on device.
     *
     * @param connection An established connection to the device
     * @param username Username for the user that should have display name updated
     * @param displayName New display name
     * @throws USER_DOES_NOT_EXIST if the specified user does not exist on the device (see note above)
     * @throws BLOCKED_BY_DEVICE_CONFIGURATION if the device configuration does not allow the current user to update the specified user's display name (the
     * `IAM:SetUserDisplayName` action is not allowed for the requesting role for the `IAM:Username` user)
     * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
     */
    public abstract void updateUserDisplayName(Connection connection, String username, String displayName);

    /**
     * Update an IAM user's username on device.
     *
     * @param connection An established connection to the device
     * @param username Username for the user that should have username updated
     * @param newUsername New username for the user
     * @throws USER_DOES_NOT_EXIST if the specified user does not exist on the device (see note above)
     * @throws INVALID_INPUT if username is not valid as per https://docs.nabto.com/developer/api-reference/coap/iam/post-users.html#request
     * @throws BLOCKED_BY_DEVICE_CONFIGURATION if the device configuration does not allow the current user to update the specified user's display name (the
     * `IAM:SetUserUsername` action is not allowed for the requesting role for the `IAM:Username` user)
     * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
     */
    public abstract void renameUser(Connection connection, String username, String newUsername);

    /**
     * Delete the specified user from device.
     *
     * @param connection An established connection to the device
     *
     * @throws USER_DOES_NOT_EXIST if the specified user does not exist on the device
     * @throws BLOCKED_BY_DEVICE_CONFIGURATION if the device configuration does not allow deleting this user (the
     * `IAM:DeleteUser` action for the `IAM:Username` attribute is not allowed for the requesting role)
     * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
     */
    public abstract void deleteUser(Connection connection, String username);
}
