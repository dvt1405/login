package com.kt.altitudekotlin.model

import com.google.gson.annotations.SerializedName

class EndPoints(
    @SerializedName("notificationEndpoint")
    var notificationEndpoint: String? = null,
    @SerializedName("weatherEndpoint")
    var weatherEndpoint: String? = null,
    @SerializedName("deviceAuthEndpoint")
    var deviceAuthEndpoint: String? = null,
    @SerializedName("annoucementEndpoint")
    var annoucementEndpoint: String? = null,
    @SerializedName("propertyEndpoint")
    var propertyEndpoint: String? = null,
    @SerializedName("guestAppConfigEndpoint")
    var guestAppConfigEndpoint: String? = null,
    @SerializedName("appFeedbackEndpoint")
    var appFeedbackEndpoint: String? = null,
    @SerializedName("stayFeedbackEndPoint")
    var stayFeedbackEndPoint: String? = null,
    @SerializedName("eventLocalEndPoint")
    var eventLocalEndPoint: String? = null,
    @SerializedName("miniBarEndPoint")
    var miniBarEndPoint: String? = null
) {
}