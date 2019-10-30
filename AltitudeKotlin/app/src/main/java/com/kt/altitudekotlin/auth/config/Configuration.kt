package com.kt.altitudekotlin.auth.config

import android.content.Context
import com.kt.altitudekotlin.auth.ConnectionBuilderForTesting
import net.openid.appauth.connectivity.ConnectionBuilder
import java.lang.ref.WeakReference

class Configuration private constructor(){

    init {

    }
    companion object {
        val TAG: String = "Configuration"
        val PREFS_NAME = " config"
        var  instance:WeakReference<Configuration> = WeakReference<Configuration>(null)
        @JvmStatic
        fun getInstance(context: Context): Configuration {
            var config = instance.get()
            if(config == null){
                config = Configuration()
                instance = WeakReference<Configuration>(config)
            }
            return config
        }

    }
    fun getConnectionBuilder(): ConnectionBuilder {
        return ConnectionBuilderForTesting.INSTANCE
    }
}