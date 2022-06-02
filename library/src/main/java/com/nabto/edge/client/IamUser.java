package com.nabto.edge.client;

import org.jetbrains.annotations.*;
import com.fasterxml.jackson.annotation.*;

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

    public IamUser(@NotNull String username) {
        this(username, null, null, null, null);
    }

    @NotNull
    public String getUsername() {
        return username;
    }

    @Nullable
    public String getDisplayName() {
        return displayName;
    }

    @Nullable
    public String getFingerprint() {
        return fingerprint;
    }

    @Nullable
    public String getSct() {
        return sct;
    }

    @Nullable
    public String getRole() {
        return role;
    }
}