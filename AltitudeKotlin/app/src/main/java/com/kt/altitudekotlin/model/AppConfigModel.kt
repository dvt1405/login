package com.kt.altitudekotlin.model

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kt.altitudekotlin.util.TextUtils

class AppConfigModel private constructor() {
    init {
//
    }

    @SerializedName("Auth")
    lateinit var auth: Auth
    @SerializedName("Endpoints")
    lateinit var endPoints: EndPoints
    companion object {
        private var appConfigModel: AppConfigModel? = null
        @JvmStatic
        fun getInstance(context: Context): AppConfigModel? {
            val filePath = "config/config.json"
            if (appConfigModel == null) {
                appConfigModel = Gson().fromJson(
                    TextUtils.readFromFile(context, filePath),
                    AppConfigModel::class.java
                )
            }
            return appConfigModel
        }
    }

}