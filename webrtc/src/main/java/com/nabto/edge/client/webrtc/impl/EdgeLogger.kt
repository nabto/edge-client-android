package com.nabto.edge.client.webrtc.impl

import android.util.Log
import com.nabto.edge.client.webrtc.EdgeWebrtcLogLevel
import org.webrtc.Loggable
import org.webrtc.Logging

internal object EdgeLogger: Loggable {
    private const val tag = "EdgeWebRTC"
    var logLevel = EdgeWebrtcLogLevel.WARNING

    fun error(msg: String) {
        log(EdgeWebrtcLogLevel.ERROR, msg)
    }

    fun warning(msg: String) {
        log(EdgeWebrtcLogLevel.WARNING, msg)
    }

    fun info(msg: String) {
        log(EdgeWebrtcLogLevel.INFO, msg)
    }

    fun verbose(msg: String) {
        log(EdgeWebrtcLogLevel.VERBOSE, msg)
    }

    private fun log(msgLevel: EdgeWebrtcLogLevel, msg: String) {
        if (msgLevel.ordinal <= logLevel.ordinal) {
            when (msgLevel) {
                EdgeWebrtcLogLevel.ERROR -> Log.e(tag, msg)
                EdgeWebrtcLogLevel.WARNING -> Log.w(tag, msg)
                EdgeWebrtcLogLevel.INFO -> Log.i(tag, msg)
                EdgeWebrtcLogLevel.VERBOSE -> Log.v(tag, msg)
            }
        }
    }

    override fun onLogMessage(p0: String?, p1: Logging.Severity?, p2: String?) {
        // @TODO: Make WebRTC internal logging activate separately, e.g. with a enableWebRTCLogs() function
        // Right now libwebrtc logs only get printed when log level is >= verbose.
        if (logLevel == EdgeWebrtcLogLevel.VERBOSE) {
            verbose("[libwebrtc $p1]: $p0")
        }
    }
}