package com.nabto.edge.iamutil.ktx
import java.util.Optional
import kotlinx.coroutines.*
import com.nabto.edge.iamutil.*
import com.nabto.edge.client.Connection

internal suspend fun <T> iamWrapper(
    register: (cb: IamCallback<T>) -> Unit
): Optional<T> = suspendCancellableCoroutine<Optional<T>> { continuation ->
    val callback = { error: IamError, opt: Optional<T> ->
        if (error == IamError.NONE) {
            continuation.resumeWith(Result.success(opt))
        } else {
            val cause = IamException(error)
            continuation.resumeWith(Result.failure(cause))
        }
    }
    register(callback)
}

/**
 * Perform Local Open pairing, requesting the specified username.
 *
 * Local open pairing uses the trusted local network (LAN) pairing mechanism. No password is required for pairing and no
 * invitation is needed, anybody on the LAN can initiate pairing.
 *
 * This function is meant to be used in a Kotlin coroutine to suspend execution until the operation has completed.
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
suspend fun IamUtil.awaitPairLocalOpen(
    connection: Connection,
    desiredUsername: String,
) {
    iamWrapper<Unit> { callback ->
        this@awaitPairLocalOpen.pairLocalOpenCallback(connection, desiredUsername, callback)
    }
}

/**
 * Perform Local Initial pairing, assigning the default initial username configured on the device (typically "admin").
 *
 * In this mode, the initial user can be paired on the local network without providing a username or password - and
 * only the initial user. This is a typical bootstrap scenario to pair the admin user (device owner).
 *
 * This function is meant to be used in a Kotlin coroutine to suspend execution until the operation has completed.
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
suspend fun IamUtil.awaitPairLocalInitial(
    connection: Connection,
) {
    iamWrapper<Unit> { callback ->
        this@awaitPairLocalInitial.pairLocalInitialCallback(connection, callback)
    }
}

/**
 * Perform Password Open pairing, requesting the specified username and authenticating using the specified password.
 *
 * In this mode a device has set a password which can be used in the pairing process to grant a client access to the
 * device. The client can pair remotely to the device if necessary; it is not necessary to be on the same LAN.
 *
 * This function is meant to be used in a Kotlin coroutine to suspend execution until the operation has completed.
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
suspend fun IamUtil.awaitPairPasswordOpen(
    connection: Connection,
    desiredUsername: String,
    password: String,
) {
    iamWrapper<Unit> { callback ->
        this@awaitPairPasswordOpen.pairPasswordOpenCallback(connection, desiredUsername, password, callback)
    }
}

/**
 * Perform Password Invite pairing, authenticating with the specified username and password.
 *
 * In the Password invite pairing mode a user is required in the system to be able to pair: An existing user (or
 * the system autonomously) creates a username and password that is somehow passed to the new user (an invitation).
 *
 * This function is meant to be used in a Kotlin coroutine to suspend execution until the operation has completed.
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
suspend fun IamUtil.awaitPairPasswordInvite(
    connection: Connection,
    username: String,
    password: String,
) {
    iamWrapper<Unit> { callback ->
        this@awaitPairPasswordInvite.pairPasswordInviteCallback(connection, username, password, callback)
    }
}

/**
 * Retrieve device information that typically does not need a paired user.
 *
 * This function is meant to be used in a Kotlin coroutine to suspend execution until the operation has completed.
 *
 * @param connection An established connection to the device
 *
 * @throws BLOCKED_BY_DEVICE_CONFIGURATION if the device configuration does not allow retrieving this list (the
 * `IAM:GetPairing` action is not set for the Unpaired role)
 * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
 */
suspend fun IamUtil.awaitGetDeviceDetails(
    connection: Connection,
): DeviceDetails {
    return iamWrapper<DeviceDetails>({ callback ->
        this@awaitGetDeviceDetails.getDeviceDetailsCallback(connection, callback)
    }).get()
}

/**
 * Query if the current user is paired or not on a specific device.
 *
 * This function is meant to be used in a Kotlin coroutine to suspend execution until the operation has completed.
 *
 * @param connection An established connection to the device
 * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
 * @return true iff the current user is paired with the device
 */
suspend fun IamUtil.awaitIsCurrentUserPaired(
    connection: Connection,
): Boolean {
    return iamWrapper<Boolean>({ callback ->
        this@awaitIsCurrentUserPaired.isCurrentUserPairedCallback(connection, callback)
    }).get()
}

/**
 * Get details about a specific user.
 *
 * This function is meant to be used in a Kotlin coroutine to suspend execution until the operation has completed.
 *
 * @param connection An established connection to the device
 * @param username Username of the user to get
 *
 * @throws USER_DOES_NOT_EXIST if the user does not exist on the device
 * @throws BLOCKED_BY_DEVICE_CONFIGURATION if the device configuration does not allow retrieving this user  (the
 * `IAM:GetUser` action is not set for the requesting role)
 * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
 * @return an IamUser instance describing the requested user
 */
suspend fun IamUtil.awaitGetUser(
    connection: Connection,
    username: String,
): IamUser {
    return iamWrapper<IamUser>({ callback ->
        this@awaitGetUser.getUserCallback(connection, username, callback)
    }).get()
}

/**
 * Get details about the user that has opened the current connection to the device.
 *
 * This function is meant to be used in a Kotlin coroutine to suspend execution until the operation has completed.
 *
 * @param connection An established connection to the device
 *
 * @throws USER_DOES_NOT_EXIST if the current user is not paired with the device.
 * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
 * @return an IamUser instance describing the current user
 */
suspend fun IamUtil.awaitGetCurrentUser(
    connection: Connection,
): IamUser {
    return iamWrapper<IamUser>({ callback ->
        this@awaitGetCurrentUser.getCurrentUserCallback(connection, callback)
    }).get()
}

/**
 * Create an IAM user on device.
 *
 * This function is meant to be used in a Kotlin coroutine to suspend execution until the operation has completed.
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
suspend fun IamUtil.awaitCreateUser(
    connection: Connection,
    username: String,
    password: String,
    role: String,
) {
    iamWrapper<Unit> { callback ->
        this@awaitCreateUser.createUserCallback(connection, username, password, role, callback);
    }
}

/**
 * Update an IAM user's password on device.
 *
 * This function is meant to be used in a Kotlin coroutine to suspend execution until the operation has completed.
 *
 * @param connection An established connection to the device
 * @param username Username for the user that should have password updated
 * @param password New password for the user
 * @throws BLOCKED_BY_DEVICE_CONFIGURATION if the device configuration does not allow the current user to update the specified user's password (the
 * `IAM:SetUserPassword` action is not allowed for the requesting role for the `IAM:Username` user)
 * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
 */
suspend fun IamUtil.awaitUpdateUserPassword(
    connection: Connection,
    username: String,
    password: String,
) {
    iamWrapper<Unit> { callback ->
        this@awaitUpdateUserPassword.updateUserPasswordCallback(connection, username, password, callback)
    }
}

/**
 * Update an IAM user's role on device.
 *
 * Known issue: This function currently assumes the user exists. To be able to interpret the
 * ROLE_DOES_NOT_EXIST code correctly, this assumption most hold. Later it can gracefully handle
 * non-existing users
 *
 * This function is meant to be used in a Kotlin coroutine to suspend execution until the operation has completed.
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
suspend fun IamUtil.awaitUpdateUserRole(
    connection: Connection,
    username: String,
    role: String,
) {
    iamWrapper<Unit> { callback ->
        this@awaitUpdateUserRole.updateUserRoleCallback(connection, username, role, callback)
    }
}

/**
 * Update an IAM user's display name on device.
 *
 * This function is meant to be used in a Kotlin coroutine to suspend execution until the operation has completed.
 *
 * @param connection An established connection to the device
 * @param username Username for the user that should have display name updated
 * @param displayName New display name
 * @throws USER_DOES_NOT_EXIST if the specified user does not exist on the device (see note above)
 * @throws BLOCKED_BY_DEVICE_CONFIGURATION if the device configuration does not allow the current user to update the specified user's display name (the
 * `IAM:SetUserDisplayName` action is not allowed for the requesting role for the `IAM:Username` user)
 * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
 */
suspend fun IamUtil.awaitUpdateUserDisplayName(
    connection: Connection,
    username: String,
    displayName: String,
) {
    iamWrapper<Unit> { callback ->
        this@awaitUpdateUserDisplayName.updateUserDisplayNameCallback(connection, username, displayName, callback)
    }
}

/**
 * Update an IAM user's username on device.
 *
 * This function is meant to be used in a Kotlin coroutine to suspend execution until the operation has completed.
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
suspend fun IamUtil.awaitRenameUser(
    connection: Connection,
    username: String,
    newUsername: String,
) {
    iamWrapper<Unit> { callback ->
        this@awaitRenameUser.renameUserCallback(connection, username, newUsername, callback)
    }
}

/**
 * Delete the specified user from device.
 *
 * This function is meant to be used in a Kotlin coroutine to suspend execution until the operation has completed.
 *
 * @param connection An established connection to the device
 * @param username Username of the user to delete
 *
 * @throws USER_DOES_NOT_EXIST if the specified user does not exist on the device
 * @throws BLOCKED_BY_DEVICE_CONFIGURATION if the device configuration does not allow deleting this user (the
 * `IAM:DeleteUser` action for the `IAM:Username` attribute is not allowed for the requesting role)
 * @throws IAM_NOT_SUPPORTED if Nabto Edge IAM is not supported by the device
 */
suspend fun IamUtil.awaitDeleteUser(
    connection: Connection,
    username: String,
) {
    iamWrapper<Unit> { callback ->
        this@awaitDeleteUser.deleteUserCallback(connection, username, callback)
    }
}
