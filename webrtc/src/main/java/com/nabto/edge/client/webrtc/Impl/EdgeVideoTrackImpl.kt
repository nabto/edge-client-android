package com.nabto.edge.client.webrtc

import org.webrtc.VideoTrack

class EdgeVideoTrackImpl(val track: VideoTrack) : EdgeVideoTrack {
    override fun add(view: EdgeVideoView) {
        track.addSink(view)
    }

    override fun remove(view: EdgeVideoView) {
        track.removeSink(view)
    }

    override val type: EdgeMediaTrackType
        get() = EdgeMediaTrackType.VIDEO
}
