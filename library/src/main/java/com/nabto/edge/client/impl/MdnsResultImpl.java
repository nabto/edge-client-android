package com.nabto.edge.client.impl;

import com.nabto.edge.client.MdnsResult;
import com.nabto.edge.client.MdnsResult.Action;
import java.util.HashMap;

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
        return new HashMap<String, String>();
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
