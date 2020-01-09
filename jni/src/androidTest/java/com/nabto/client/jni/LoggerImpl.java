package com.nabto.client.jni;

import android.util.Log;
import com.nabto.client.jni.*;

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
