package com.nabto.edge.client.webrtc.impl

import com.nabto.edge.client.webrtc.EdgeMediaTrackType
import com.nabto.edge.client.webrtc.EdgeVideoTrack
import com.nabto.edge.client.webrtc.EdgeVideoView
import org.webrtc.VideoTrack

internal class EdgeVideoTrackImpl(val track: VideoTrack) : EdgeVideoTrack {
    override fun add(view: EdgeVideoView) {
        track.addSink(view)
    }

    override fun remove(view: EdgeVideoView) {
        track.removeSink(view)
    }

    override val type: EdgeMediaTrackType
        get() = EdgeMediaTrackType.VIDEO
}
