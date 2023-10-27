package com.nabto.edge.client.impl;

import android.util.Log;

import com.nabto.edge.client.MdnsResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class MdnsResultImpl implements MdnsResult {
    private final String deviceId;
    private final String productId;
    private final String serviceInstanceName;
    private final java.util.Map<String, String> txtItems;
    private final Action action;

    public MdnsResultImpl(com.nabto.edge.client.swig.MdnsResult result)
    {
        deviceId = result.getDeviceId();
        productId = result.getProductId();
        serviceInstanceName = result.getServiceInstanceName();
        txtItems = copyTxtItems(result);
        action = copyAction(result);
        result.delete();
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public String getProductId() {
        return productId;
    }

    @Override
    public String getServiceInstanceName() {
        return serviceInstanceName;
    }

    @Override
    public java.util.Map<String, String> getTxtItems() {
        return txtItems;
    }

    @Override
    public Action getAction() {
        return action;
    }

    private java.util.Map<String, String> copyTxtItems(com.nabto.edge.client.swig.MdnsResult result) {
        String jsonFromDevice = result.getTxtItems();
        HashMap<String, String> items = new HashMap<>();
        try {
            JSONObject json = new JSONObject(jsonFromDevice);
            Iterator<String> keys = json.keys();
            while(keys.hasNext()) {
                String key = keys.next();
                try {
                    String value = json.getString(key);
                    items.put(key, value);
                } catch (JSONException e) {
                    Log.w("nabto", "Error accessing key " + key + " in mDNS result JSON from device: " + e);
                }
            }
        } catch (JSONException e) {
            Log.w("nabto", "Error parsing mDNS result JSON from device: " + e);
        }
        return items;
    }

    private Action copyAction(com.nabto.edge.client.swig.MdnsResult result) {
        com.nabto.edge.client.swig.MdnsResult.Action action = result.getAction();
        if (action == com.nabto.edge.client.swig.MdnsResult.Action.ADD) {
            return Action.ADD;
        } else if (action == com.nabto.edge.client.swig.MdnsResult.Action.UPDATE) {
            return Action.UPDATE;
        } else if (action == com.nabto.edge.client.swig.MdnsResult.Action.REMOVE) {
            return Action.REMOVE;
        }
        //  never here
        return Action.UPDATE;
    }

}
