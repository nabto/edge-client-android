package com.nabto.edge.client.webrtc.impl

import com.nabto.edge.client.webrtc.EdgeAudioTrack
import com.nabto.edge.client.webrtc.EdgeMediaTrackType
import org.webrtc.AudioTrack
import java.lang.IllegalStateException

internal class EdgeAudioTrackImpl(private val track: AudioTrack) : EdgeAudioTrack {
    override val type: EdgeMediaTrackType
        get() = EdgeMediaTrackType.AUDIO

    override fun setEnabled(enabled: Boolean) {
        try {
            track.setEnabled(enabled)
        } catch (e: IllegalStateException) {
            EdgeLogger.warning("EdgeAudioTrack.setEnabled threw IllegalStateException ${e.message}")
        }
    }

    override fun setVolume(volume: Double) {
        track.setVolume(volume)
    }
}
