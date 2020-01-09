package com.nabto.edge.client.swig;

import android.util.Log;

class LoggerImpl extends Logger {
    public void log(LogMessage message) {
        String msg = message.getMessage();
        Log.v("nabto", msg);
        logs++;
    }

    int logs = 0;
    public int getLogs() {
        return logs;
    }
}
