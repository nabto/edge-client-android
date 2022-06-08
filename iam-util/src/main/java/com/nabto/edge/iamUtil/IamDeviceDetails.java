package com.nabto.edge.iamUtil;

import org.jetbrains.annotations.*;
import com.fasterxml.jackson.annotation.*;

public class IamDeviceDetails {
    @JsonProperty(value = "Modes", required = true)
    private String[] modes;

    @JsonProperty(value = "NabtoVersion", required = true)
    private String nabtoVersion;

    @JsonProperty(value = "AppVersion", required = false)
    private String appVersion;

    @JsonProperty(value = "AppName", required = false)
    private String appName;

    @JsonProperty(value = "ProductId", required = true)
    private String productId;

    @JsonProperty(value = "DeviceId", required = true)
    private String deviceId;

    @JsonCreator
    public IamDeviceDetails(
        @JsonProperty(value = "Modes",        required = true ) @NotNull String[] modes,
        @JsonProperty(value = "NabtoVersion", required = false) @NotNull String nabtoVersion,
        @JsonProperty(value = "AppVersion",   required = false) String appVersion,
        @JsonProperty(value = "AppName",      required = false) String appName,
        @JsonProperty(value = "ProductId",    required = false) @NotNull String productId,
        @JsonProperty(value = "DeviceId",     required = false) @NotNull String deviceId
    ) {
        this.modes = modes;
        this.nabtoVersion = nabtoVersion;
        this.appVersion = appVersion;
        this.appName = appName;
        this.productId = productId;
        this.deviceId = deviceId;
    }

    @NotNull
    public String[] getModes() {
        return modes;
    }

    @NotNull
    public String getNabtoVersion() {
        return nabtoVersion;
    }

    @Nullable
    public String getAppVersion() {
        return appVersion;
    }

    @Nullable
    public String getAppName() {
        return appName;
    }

    @NotNull
    public String getProductId() {
        return productId;
    }

    @NotNull
    public String getDeviceId() {
        return deviceId;
    }
}
