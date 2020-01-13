package com.nabto.edge.heatpump.heatpump

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class HeatPumpCoapState {
    @JsonProperty("Mode", required = true) var mode : String = ""
    @JsonProperty("Power", required = true) var power : Boolean = false
    @JsonProperty("Target", required = true) var target : Double = 0.0
    @JsonProperty("Temperature", required = true) var temperature: Double = 0.0
}