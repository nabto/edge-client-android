package com.nabto.edge.client;

/**
 * The result of an mDNS discovery request.
 */
public interface MdnsResult {
    /**
     * Action which is associated with a result. This is used together
     * with the service instance name to manipulate the list of device.
     *
     * - `ADD`: Adding a new result
     * - `UPDATE`: Updating existing result
     * - `REMOVE`: Removing existing result
     */
    enum Action {
        ADD,
        UPDATE,
        REMOVE
    }

    /**
     * Get the device ID. If not set the empty string is returned.
     *
     * @return The device ID.
     */
    String getDeviceId();

    /**
     * Get the product ID, if not set the empty string is returned.
     *
     * @return The product ID
     */
    String getProductId();

    /**
     * Get the service instance name used to distinguish results.
     *
     * If the application is maintaning a list of mDNS results the Service
     * instance name is used as the key. The action ADD, UPDATE,
     * REMOVE is then used to see how the list should be manipulated.
     *
     * @return The service instance name string.
     */
    String getServiceInstanceName();

    /**
     * Txt Items as a map, if there's no TXT items the map is empty.
     *
     * @return The TXT items as a map
     */
    java.util.Map<String, String> getTxtItems();

    /**
     * Return the action associated with the mdns result.
     *
     * @return The action.
     */
    Action getAction();
}
