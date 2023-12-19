package com.VarsityCollege.Aves


import kotlinx.serialization.Serializable


@Serializable
data class Hotspot(
    val locId: String,
    val locName: String,
    val countryCode: String,
    val lat: Double,
    val lng: Double,
    val comName: String?,
    val sciName: String?,
    val obsDt: String,
)