package com.kt.altitudekotlin.api

import com.kt.altitudekotlin.api.request.DeviceRequest
import com.kt.altitudekotlin.api.response.DeviceResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiProperties {
    @POST("guests/me/devices")
    fun postDevice(@Body request: DeviceRequest): Call<DeviceResponse>
    @POST("guests/me/devices/{deviceId}")
    fun putDevice(@Path("deviceId") deviceId: String, @Body request: DeviceRequest): Call<DeviceResponse>
}