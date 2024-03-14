package com.nabto.edge.client.webrtc.impl

import android.content.Context
import androidx.startup.Initializer
import com.nabto.edge.client.Connection
import com.nabto.edge.client.webrtc.EdgeVideoView
import com.nabto.edge.client.webrtc.EdgeWebrtcLogLevel
import com.nabto.edge.client.webrtc.EdgeWebrtcManager
import com.nabto.edge.client.webrtc.EdgeWebrtcConnection
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.Logging
import org.webrtc.PeerConnectionFactory
import org.webrtc.RendererCommon

internal class EdgeWebrtcManagerInternal : EdgeWebrtcManager {
    companion object {
        val eglBase: EglBase = EglBase.create()
        val instance = EdgeWebrtcManagerInternal()
        lateinit var peerConnectionFactory: PeerConnectionFactory

        fun initialize(context: Context) {
            val initOpts = PeerConnectionFactory.InitializationOptions.builder(context).apply {
                setInjectableLogger(EdgeLogger, Logging.Severity.LS_INFO)
            }.createInitializationOptions()
            PeerConnectionFactory.initialize(initOpts)

            val encoderFactory = DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true)
            val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)
            peerConnectionFactory = PeerConnectionFactory.builder().apply {
                setVideoEncoderFactory(encoderFactory)
                setVideoDecoderFactory(decoderFactory)
            }.createPeerConnectionFactory()
        }
    }

    override fun setLogLevel(logLevel: EdgeWebrtcLogLevel) {
        EdgeLogger.logLevel = logLevel
    }

    override fun initVideoView(view: EdgeVideoView) {
        view.init(eglBase.eglBaseContext, object : RendererCommon.RendererEvents {
            override fun onFirstFrameRendered() {}
            override fun onFrameResolutionChanged(p0: Int, p1: Int, p2: Int) {}
        })
    }

    override fun createRTCConnection(conn: Connection): EdgeWebrtcConnection {
        return EdgeWebrtcConnectionImpl(conn)
    }
}

class EdgeWebrtcInitializer : Initializer<EdgeWebrtcManager> {
    override fun create(context: Context): EdgeWebrtcManager {
        EdgeWebrtcManagerInternal.initialize(context)
        return EdgeWebrtcManagerInternal.instance
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}