package com.nabto.edge.client;

import org.jetbrains.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    public IamDeviceDetails(
        @NotNull String[] modes,
        @NotNull String nabtoVersion,
        String appVersion,
        String appName,
        @NotNull String productId,
        @NotNull String deviceId
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
