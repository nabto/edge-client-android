package com.nabto.edge.client.webrtc

import android.content.Context
import android.util.AttributeSet
import com.nabto.edge.client.Connection
import com.nabto.edge.client.webrtc.impl.EdgeWebrtcManagerInternal
import io.getstream.webrtc.android.ui.VideoTextureViewRenderer

// @TODO: Make our own TextureViewRenderer implementation?
// @TODO: Make a Jetpack Composable View?
class EdgeVideoView(
    context: Context,
    attrs: AttributeSet? = null
) : VideoTextureViewRenderer(context, attrs) {
}

/**
 * Track types used to identify if a track is Video or Audio.
 */
enum class EdgeMediaTrackType {
    AUDIO,
    VIDEO
}

/**
 * Interface used to represent all Media Tracks
 */
interface EdgeMediaTrack {
    val type: EdgeMediaTrackType
}

/**
 * Log levels to use in the underlying SDK
 */
enum class EdgeWebrtcLogLevel {
    ERROR,
    WARNING,
    INFO,
    VERBOSE
}

/**
 * Error class for errors emitted by the onErrorCallback
 */
sealed class EdgeWebrtcError : Error() {
    /**
     * The signaling stream could not be established properly.
     */
    class SignalingFailedToInitialize() : EdgeWebrtcError()

    /**
     * Reading from the Signaling Stream failed
     */
    class SignalingFailedRecv() : EdgeWebrtcError()

    /**
     * An invalid signaling message was received
     */
    class SignalingInvalidMessage() : EdgeWebrtcError()

    /**
     * The remote description received from the other peer was invalid
     */
    class SetRemoteDescriptionError() : EdgeWebrtcError()

    /**
     * Failed to send an Answer on the signaling stream
     */
    class SendAnswerError() : EdgeWebrtcError()

    /**
     * A invalid ICE candidate was received from the other peer
     */
    class ICECandidateError() : EdgeWebrtcError()

    /**
     * The RTC PeerConnection could not be created
     */
    class ConnectionInitError() : EdgeWebrtcError()
}

/**
 * Video Track representing a Media Track of type Video
 */
interface EdgeVideoTrack : EdgeMediaTrack {
    /**
     * Add a Video View to the track
     *
     * @param view [in] The view to add
     * @throws IllegalArgumentException if [view] is null.
     */
    fun add(view: EdgeVideoView)

    /**
     * remove a Video View to the track.
     * If the EdgeVideoView was not attached to this track, this function is a no-op.
     * This function does not throw any exceptions.
     */
    fun remove(view: EdgeVideoView)
}

/**
 * Audio Track representing a Media Track of type Audio
 */
interface EdgeAudioTrack : EdgeMediaTrack {
    // TODO: add @throws docs
    /**
     * Enable or disable the Audio track
     *
     * @param enabled [in] Boolean determining if the track is enabled
     */
    fun setEnabled(enabled: Boolean)

    /**
     * Set the volume of the Audio track.
     * This function does not throw any exceptions.
     *
     * @param volume [in] The volume to set
     */
    fun setVolume(volume: Double)
}

/**
 * Callback invoked when a WebRTC connection has been closed
 */
typealias OnClosedCallback = () -> Unit

/**
 * Callback invoked when the remote peer has added a Track to the WebRTC connection
 *
 * @param EdgeMediaTrack [in] The newly added Track
 */
typealias OnTrackCallback = (EdgeMediaTrack) -> Unit

/**
 * Callback invoked when an error occurs in the WebRTC connection
 *
 * @param EdgeWebRTCError [in] The Error that occurred
 */
typealias OnErrorCallback = (EdgeWebrtcError) -> Unit


/**
 * Main Connection interface used to connect to a device and interact with it.
 */
interface EdgeWebrtcConnection {
    /**
     * Set callback to be invoked when the WebRTC connection is closed
     *
     * @param cb The callback to set
     */
    fun onClosed(cb: OnClosedCallback)

    /**
     * Set callback to be invoked when a new track is available on the WebRTC connection
     *
     * @param cb The callback to set
     */
    fun onTrack(cb: OnTrackCallback)

    /**
     * Set callback to be invoked when an error occurs on the WebRTC connection.
     *
     * @param cb The callback to set
     */
    fun onError(cb: OnErrorCallback)


    // TODO: add @throws docs
    /**
     * Establish a WebRTC connection to the other peer
     */
    suspend fun connect()

    // TODO: add @throws docs
    /**
     * Close a connected WebRTC connection.
     */
    suspend fun connectionClose()
}

/**
 * Manager interface to keep track of global WebRTC state
 */
interface EdgeWebrtcManager {

    /**
     * Set the log level to use by the underlying SDK
     *
     * @param logLevel [in] The log level to set
     */
    fun setLogLevel(logLevel: EdgeWebrtcLogLevel)

    // TODO: add @throws docs
    /**
     * Initialize a video view to use for video tracks.
     *
     * @param view [in] The view to initialize
     */
    fun initVideoView(view: EdgeVideoView)

    // TODO: add @throws docs
    /**
     * Create a new WebRTC connection instance using a preexisting Nabto Edge Connection for signaling.
     *
     * Only one WebRTC connection can exist on a Nabto Edge Connection at a time.
     *
     * @param conn [in] The Nabto Edge Connection to use for signaling
     * @return The created EdgeWebrtcConnection object
     */
    fun createRTCConnection(conn: Connection): EdgeWebrtcConnection

    companion object {
        fun getInstance(): EdgeWebrtcManager = EdgeWebrtcManagerInternal.instance
    }
}
