package com.kt.altitudekotlin.model

import com.google.gson.annotations.SerializedName

data class Auth(
    @SerializedName("AppToken")
    val AppToken: String,
    @SerializedName("AppID")
    val AppID: String,
    @SerializedName("dClientId")
    val ClientID: String,
    @SerializedName("dClientSecret")
    val ClientSecret: String,
    @SerializedName("dRedirectUri")
    val RedirectUri: String,
    @SerializedName("dRefrshTokenUrl")
    val RefreshTokenUrl: String,
    @SerializedName("dTenantId")
    val TenantID: String?,
    @SerializedName("OpenIdConfigurationUrl")
    val OpenIdConfigurationUrl: String
) {
    fun getRedirectUriHttps():String{
        return "https://"+RedirectUri
    }
}