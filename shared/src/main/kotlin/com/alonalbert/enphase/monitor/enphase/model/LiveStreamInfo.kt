package com.alonalbert.enphase.monitor.enphase.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LiveStreamInfo(
    @SerialName("live_stream_duration") val liveStreamDuration: Int,
    @SerialName("live_stream_topic") val liveStreamTopic: String,
    @SerialName("dr_event_mode") val drEventMode: String,
    @SerialName("has_load_controls") val hasLoadControls: Boolean,
    @SerialName("timeout") val timeout: Int,
    @SerialName("aws_iot_endpoint") val awsIotEndpoint: String,
    @SerialName("aws_authorizer") val awsAuthorizer: String,
    @SerialName("aws_token_key") val awsTokenKey: String,
    @SerialName("aws_token_value") val awsTokenValue: String,
    @SerialName("aws_digest") val awsDigest: String
)
