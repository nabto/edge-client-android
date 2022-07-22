package com.nabto.edge.iamutil;

import org.jetbrains.annotations.*;
import com.fasterxml.jackson.annotation.*;

/**
 * This struct contains information about a user on a Nabto Edge Embedded device.
 */
public class IamUser {
    @JsonProperty(value = "Username", required = true)
    public String username;

    @JsonProperty(value = "DisplayName", required = false)
    public String displayName;

    @JsonProperty(value = "Fingerprint", required = false)
    public String fingerprint;

    @JsonProperty(value = "Sct", required = false)
    public String sct;

    @JsonProperty(value = "Role", required = false)
    public String role;

    @JsonCreator
    public IamUser(
        @JsonProperty(value = "Username",    required = true ) @NotNull String username,
        @JsonProperty(value = "DisplayName", required = false) String displayName,
        @JsonProperty(value = "Fingerprint", required = false) String fingerprint,
        @JsonProperty(value = "Sct",         required = false) String sct,
        @JsonProperty(value = "Role",        required = false) String role
    ) {
        this.username = username;
        this.displayName = displayName;
        this.fingerprint = fingerprint;
        this.sct = sct;
        this.role = role;
    }

    /**
     * Create an IamUser instance with the specified username.
     * @param username The username of this user.
     */
    public IamUser(@NotNull String username) {
        this(username, null, null, null, null);
    }

    @NotNull
    /**
     * Get the username of this IAM user.
     * @return the username of the IAM user.
     */
    public String getUsername() {
        return username;
    }

    @Nullable
    /**
     * Get the display name of this IAM user.
     * @return The display name of the IAM user. May return null if there is no display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    @Nullable
    /**
     * Get the public key fingerprint of this IAM user.
     * @return The public key fingerprint of the IAM user. May return null.
     */
    public String getFingerprint() {
        return fingerprint;
    }

    @Nullable
    /**
     * Get the server connect token for this user.
     * @return The server connect token of the IAM user. May return null.
     */
    public String getSct() {
        return sct;
    }

    @Nullable
    /**
     * Get the role of this user.
     * @return The role of the IAM user. May return null.
     */
    public String getRole() {
        return role;
    }
}
