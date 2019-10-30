package com.kt.altitudekotlin.api.request

import android.os.Build
import com.kt.altitudekotlin.App
import com.kt.altitudekotlin.model.AppConfigModel

class DeviceRequest( uniqueDeviceId: String) {
    val uniqueDeviceId:String
    val model: String
    val os: String
    val packageId: String
    val appId: String
    val version: String

    init {
        this.uniqueDeviceId = uniqueDeviceId
        this.model = Build.MODEL
        this.os = "Android " + Build.VERSION.SDK_INT
        this.packageId = App.get().packageName
        this.appId = AppConfigModel.getInstance(App.get())!!.auth.AppID
        this.version = Build.VERSION.RELEASE
    }
}
