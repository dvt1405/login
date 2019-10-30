package com.kt.altitudekotlin.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kt.altitudekotlin.App
import com.kt.altitudekotlin.api.calladapter.LiveDataCallAdapterFactory
import com.kt.altitudekotlin.auth.config.AuthStateManager
import com.kt.altitudekotlin.model.AppConfigModel
import net.openid.appauth.AuthState
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class APIControl private constructor() {
    var apiProperty: ApiProperties

    companion object {
        private var INSTANCE: APIControl? = null
        fun getInstance(): APIControl {
            if (INSTANCE == null) {
                INSTANCE = APIControl()
            }
            return INSTANCE!!
        }
    }

    init {
        val inteceptor = HttpLoggingInterceptor()
        inteceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client: OkHttpClient = OkHttpClient.Builder().addInterceptor {
            val newRequest = it.request().newBuilder()
                .addHeader("cache-control", "no-cache")
                .addHeader(
                    "Authorization",
                    "Bearer " +AuthStateManager.getInstance(App.get()).current?.accessToken
                )
                .build()
            var current: AuthState? = AuthStateManager.getInstance(App.get()).current
            Log.d("Query AccessToken ", current?.accessToken!! + "")
            Log.d("Query IdToken ", current?.idToken!! + "")
            it.proceed(newRequest)
        }.addInterceptor(inteceptor).build()
        val gson: Gson = GsonBuilder().setLenient().create()
        val retrofit: Retrofit.Builder = Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
        apiProperty =
            retrofit.baseUrl(AppConfigModel.getInstance(App.get())!!.endPoints.propertyEndpoint!!)
                .build().create(ApiProperties::class.java!!)
    }
}