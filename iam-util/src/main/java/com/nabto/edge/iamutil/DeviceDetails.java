package com.nabto.edge.iamutil;

import org.jetbrains.annotations.*;
import com.fasterxml.jackson.annotation.*;


/**
 * This class contains detailed information about a Nabto Edge Embedded device.
 */
public class DeviceDetails {
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
    /*
     * Create an instance.
     */
    public DeviceDetails(
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
     *
     * @return Array of available pairing modes.
     */
    public String[] getModes() {
        return modes;
    }

    @NotNull
    /**
     * The version of the Nabto Edge Embedded SDK.
     *
     * @return The SDK version of the device
     */
    public String getNabtoVersion() {
        return nabtoVersion;
    }

    @Nullable
    /**
     * The vendor assigned application version.
     *
     * @return The App version of the device
     */
    public String getAppVersion() {
        return appVersion;
    }

    @Nullable
    /**
     * The vendor assigned application name.
     *
     * @return The App name of the device
     */
    public String getAppName() {
        return appName;
    }

    @NotNull
    /**
     * The device's product id.
     *
     * @return The product ID of the device
     */
    public String getProductId() {
        return productId;
    }

    @NotNull
    /**
     * The device's device id.
     *
     * @return The device ID of the device
     */
    public String getDeviceId() {
        return deviceId;
    }
}
