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

sealed class EdgeWebRTCError : Error() {
    class SignalingFailedToInitialize() : EdgeWebRTCError()
    class SignalingFailedRecv() : EdgeWebRTCError()
    class SignalingInvalidMessage() : EdgeWebRTCError()
    class SetRemoteDescriptionError() : EdgeWebRTCError()
    class SendAnswerError() : EdgeWebRTCError()
    class ICECandidateError() : EdgeWebRTCError()
    class ConnectionInitError() : EdgeWebRTCError()
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
typealias OnErrorCallback = (EdgeWebRTCError) -> Unit

interface EdgeWebrtcConnection {
    fun onConnected(cb: OnConnectedCallback)
    fun onClosed(cb: OnClosedCallback)
    fun onTrack(cb: OnTrackCallback)
    fun onError(cb: OnErrorCallback)

    fun connect()
    fun close()
}

interface EdgeWebRTCManager {
    fun setLogLevel(logLevel: EdgeWebRTCLogLevel)
    fun initVideoView(view: EdgeVideoView)
    fun createRTCConnection(conn: Connection): EdgeWebrtcConnection

    companion object {
        fun getInstance(): EdgeWebRTCManager = EdgeWebRTCManagerInternal.instance
    }
}
