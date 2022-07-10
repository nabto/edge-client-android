package com.nabto.edge.iamutil;

import org.jetbrains.annotations.*;
import com.fasterxml.jackson.annotation.*;


/**
 * This class contains detailed information about a Nabto Edge Embedded device.
 */
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
    /**
     * Create an instance.
     */
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
    /**
     * Pairing modes currently available for use by the client.
     */
    public String[] getModes() {
        return modes;
    }

    @NotNull
    /**
     * The version of the Nabto Edge Embedded SDK.
     */
    public String getNabtoVersion() {
        return nabtoVersion;
    }

    @Nullable
    /**
     * The vendor assigned application version.
     */
    public String getAppVersion() {
        return appVersion;
    }

    @Nullable
    /**
     * The vendor assigned application name.
     */
    public String getAppName() {
        return appName;
    }

    @NotNull
    /**
     * The device's product id.
     */
    public String getProductId() {
        return productId;
    }

    @NotNull
    /**
     * The device's device id.
     */
    public String getDeviceId() {
        return deviceId;
    }
}
