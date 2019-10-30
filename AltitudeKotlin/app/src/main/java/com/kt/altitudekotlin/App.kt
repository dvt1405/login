package com.kt.altitudekotlin

import android.annotation.SuppressLint
import android.app.Application
import com.kt.altitudekotlin.auth.config.AuthStateManager

class App : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit private var  app: App
        fun get() = app
    }

    override fun onCreate() {
        super.onCreate()
        app = this
    }
    fun isSignIn(): Boolean = AuthStateManager.getInstance(this).current?.isAuthorized?:false
}