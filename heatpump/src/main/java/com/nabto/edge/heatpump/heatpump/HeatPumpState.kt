package com.nabto.edge.heatpump.heatpump

data class HeatPumpState(var mode : String,
                         var power : Boolean,
                         var target : Double,
                         var temperature : Double)
{

}