package com.nabto.edge.client.webrtc

import android.content.Context
import android.util.AttributeSet
import com.nabto.edge.client.Connection
import com.nabto.edge.client.webrtc.impl.EdgeWebrtcConnectionImpl
import io.getstream.webrtc.android.ui.VideoTextureViewRenderer
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory
import org.webrtc.RendererCommon

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

class EdgeWebRTC {
    companion object {
        private val eglBase: EglBase = EglBase.create()
        internal lateinit var peerConnectionFactory: PeerConnectionFactory
        val eglBaseContext: EglBase.Context get() = eglBase.eglBaseContext

        fun initVideoView(view: EdgeVideoView) {
            view.init(eglBaseContext, object : RendererCommon.RendererEvents {
                override fun onFirstFrameRendered() {}
                override fun onFrameResolutionChanged(p0: Int, p1: Int, p2: Int) {}
            })
        }

        fun create(conn: Connection, context: Context): EdgeWebrtcConnection {
            if (!this::peerConnectionFactory.isInitialized) {
                // We need to specify that we want to use h264 encoding/decoding, otherwise the SDP
                // will not reflect this.
                val encoderFactory = DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true)
                val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)

                // Make a PeerConnectionFactory.InitializationOptions builder and build the InitializationOptions.
                // Then we can initialize the static parts of the PeerConnectionFactory class
                val staticOpts = PeerConnectionFactory.InitializationOptions.builder(context).createInitializationOptions()
                PeerConnectionFactory.initialize(staticOpts)

                // Now that the static part of the class is initialized, we make a PeerConnectionFactoryBuilder
                peerConnectionFactory = PeerConnectionFactory.builder().apply {
                    setVideoEncoderFactory(encoderFactory)
                    setVideoDecoderFactory(decoderFactory)
                }.createPeerConnectionFactory()
            }

            return EdgeWebrtcConnectionImpl(conn)
        }
    }
}
