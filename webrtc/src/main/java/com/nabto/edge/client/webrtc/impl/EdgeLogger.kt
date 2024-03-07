package com.nabto.edge.client.webrtc.impl

import android.util.Log
import com.nabto.edge.client.webrtc.EdgeWebRTCLogLevel
import org.webrtc.Loggable
import org.webrtc.Logging

internal object EdgeLogger: Loggable {
    private const val tag = "EdgeWebRTC"
    var logLevel = EdgeWebRTCLogLevel.ERROR

    fun error(msg: String) {
        log(EdgeWebRTCLogLevel.ERROR, msg)
    }

    fun warning(msg: String) {
        log(EdgeWebRTCLogLevel.WARNING, msg)
    }

    fun info(msg: String) {
        log(EdgeWebRTCLogLevel.INFO, msg)
    }

    fun verbose(msg: String) {
        log(EdgeWebRTCLogLevel.VERBOSE, msg)
    }

    private fun log(msgLevel: EdgeWebRTCLogLevel, msg: String) {
        if (msgLevel.ordinal <= logLevel.ordinal) {
            when (msgLevel) {
                EdgeWebRTCLogLevel.ERROR -> Log.e(tag, msg)
                EdgeWebRTCLogLevel.WARNING -> Log.w(tag, msg)
                EdgeWebRTCLogLevel.INFO -> Log.i(tag, msg)
                EdgeWebRTCLogLevel.VERBOSE -> Log.v(tag, msg)
            }
        }
    }

    override fun onLogMessage(p0: String?, p1: Logging.Severity?, p2: String?) {
        // @TODO: Make WebRTC internal logging activate separately, e.g. with a enableWebRTCLogs() function
        // Right now libwebrtc logs only get printed when log level is >= verbose.
        if (logLevel == EdgeWebRTCLogLevel.VERBOSE) {
            verbose("[libwebrtc $p1]: $p0")
        }
    }
}