package com.nabto.edge.client;

import org.jetbrains.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IamUser {
    @JsonProperty(value = "Username", required = true)
    private String username;

    @JsonProperty("DisplayName")
    private String displayName;

    @JsonProperty("Fingerprint")
    private String fingerprint;

    @JsonProperty("Sct")
    private String sct;

    @JsonProperty("Role")
    private String role;

    public IamUser(@NotNull String username, String displayName, String fingerprint, String sct, String role) {
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