package com.nabto.edge.client.impl;

import android.util.Log;

class Logger extends com.nabto.client.jni.Logger {
    public void log(com.nabto.client.jni.LogMessage message) {
        String msg = message.getMessage();
        if (message.getSeverity() == "trace") {
            Log.v("nabto", msg);
        } else if (message.getSeverity() == "error") {
            Log.e("nabto", msg);
        } else if (message.getSeverity() == "info") {
            Log.i("nabto", message.getMessage());
        } else if (message.getSeverity() == "warn") {
            Log.w("nabto", message.getMessage());
        } else {
            Log.v("nabto", message.getMessage());
        }
    }
}
