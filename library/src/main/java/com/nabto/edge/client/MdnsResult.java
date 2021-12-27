package com.nabto.edge.client;

/**
 * The result of an mDNS discovery request.
 */
public interface MdnsResult {
    /**
     * Action which is associated with a result. This is used together
     * with the service instance name to manipulate the list of device.
     */
    enum Action {
        ADD, UPDATE, REMOVE
    }

    /**
     * Get the device id. If not set the empty string is returned.
     * @return the device id.
     */
    String getDeviceId();

    /**
     * Get the product id, if not set the empty string is returned.
     * @return the product id
     */
    String getProductId();

    /**
     * Get the service instance name. This is used to distinguish results.
     * If the application is maintaning a list of mdns results the Service
     * instance name is used as the key. The action ADD, UPDATE,
     * REMOVE is then used to see how the list should be manipulated.
     * @return the service instance name string.
     */
    String getServiceInstanceName();

    /**
     * Txt Items as a map, if there's no txt items the map is empty.
     * @return the txt items as a map
     */
    java.util.Map<String, String> getTxtItems();

    /**
     * Return the action associated with the mdns result
     * @return the action.
     */
    Action getAction();
}
