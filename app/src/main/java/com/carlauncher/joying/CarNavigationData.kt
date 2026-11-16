package com.carlauncher.joying

data class CarNavigationData(
    val isNavigating: Boolean = false,
    val instruction: String = "",
    val distanceToManeuver: String = "",
    val maneuver: String = "STRAIGHT",
    val nextInstruction: String = "",
    val nextDistance: String = "",
    val destination: String = "",
    val remainingDistance: String = "",
    val remainingTime: String = "",
    val arrivalTime: String = ""
)