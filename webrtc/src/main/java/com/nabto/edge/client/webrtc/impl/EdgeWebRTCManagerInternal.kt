package com.nabto.edge.client.webrtc.impl

import android.content.Context
import androidx.startup.Initializer
import com.nabto.edge.client.Connection
import com.nabto.edge.client.webrtc.EdgeVideoView
import com.nabto.edge.client.webrtc.EdgeWebRTCLogLevel
import com.nabto.edge.client.webrtc.EdgeWebRTCManager
import com.nabto.edge.client.webrtc.EdgeWebrtcConnection
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.Logging
import org.webrtc.PeerConnectionFactory
import org.webrtc.RendererCommon

internal class EdgeWebRTCManagerInternal : EdgeWebRTCManager {
    companion object {
        val eglBase = EglBase.create()
        lateinit var peerConnectionFactory: PeerConnectionFactory
        lateinit var instance: EdgeWebRTCManagerInternal

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

    override fun setLogLevel(logLevel: EdgeWebRTCLogLevel) {
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

class EdgeWebRTCInitializer : Initializer<EdgeWebRTCManager> {
    override fun create(context: Context): EdgeWebRTCManager {
        EdgeWebRTCManagerInternal.initialize(context)
        return EdgeWebRTCManagerInternal.instance
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}