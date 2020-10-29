package com.nabto.edge.client.impl;

import com.nabto.edge.client.MdnsResult;
import com.nabto.edge.client.MdnsResult.Action;
import java.util.HashMap;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.Iterator;

public class MdnsResultImpl implements MdnsResult {
    private com.nabto.edge.client.swig.MdnsResult result;
    public MdnsResultImpl(com.nabto.edge.client.swig.MdnsResult result)
    {
        this.result = result;
    }

    public String getDeviceId() {
        return result.getDeviceId();
    }

    public String getProductId() {
        return result.getProductId();
    }

    public String getServiceInstanceName() {
        return result.getServiceInstanceName();
    }

    public java.util.Map<String, String> getTxtItems() {
        String j = result.getTxtItems();
        HashMap<String, String> items = new HashMap<String, String>();
        try {
            JSONObject json = new JSONObject(j);
            Iterator<String> keys = json.keys();
            while(keys.hasNext()) {
                String key = keys.next();
                try {
                    String value = json.getString(key);
                    items.put(key, value);
                } catch (JSONException e) {

                }
            }
        } catch (JSONException e) {

        }
        return items;
    }

    public Action getAction() {
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
