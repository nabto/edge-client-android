package com.nabto.edge.client.webrtc

import android.content.Context
import android.util.AttributeSet
import com.nabto.edge.client.Connection
import com.nabto.edge.client.webrtc.impl.EdgeWebRTCManagerInternal
import io.getstream.webrtc.android.ui.VideoTextureViewRenderer

// @TODO: Make our own TextureViewRenderer implementation?
// @TODO: Make a Jetpack Composable View?
class EdgeVideoView(
    context: Context,
    attrs: AttributeSet? = null
) : VideoTextureViewRenderer(context, attrs) {
}

enum class EdgeMediaTrackType {
    AUDIO,
    VIDEO
}

interface EdgeMediaTrack {
    val type: EdgeMediaTrackType
}

enum class EdgeWebRTCLogLevel {
    ERROR,
    WARNING,
    INFO,
    VERBOSE
}

interface EdgeVideoTrack : EdgeMediaTrack {
    fun add(view: EdgeVideoView)
    fun remove(view: EdgeVideoView)
}

interface EdgeAudioTrack : EdgeMediaTrack {
    fun setEnabled(enabled: Boolean)
    fun setVolume(volume: Double)
}

typealias OnConnectedCallback = () -> Unit
typealias OnClosedCallback = () -> Unit
typealias OnTrackCallback = (EdgeMediaTrack) -> Unit

interface EdgeWebrtcConnection {
    fun onConnected(cb: OnConnectedCallback)
    fun onClosed(cb: OnClosedCallback)
    fun onTrack(cb: OnTrackCallback)
}

interface EdgeWebRTCManager {
    fun setLogLevel(logLevel: EdgeWebRTCLogLevel)
    fun initVideoView(view: EdgeVideoView)
    fun createRTCConnection(conn: Connection): EdgeWebrtcConnection

    companion object {
        fun getInstance(): EdgeWebRTCManager = EdgeWebRTCManagerInternal.instance
    }
}
