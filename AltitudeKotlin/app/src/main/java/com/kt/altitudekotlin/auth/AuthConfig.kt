package com.kt.altitudekotlin.auth

import android.content.Context
import com.kt.altitudekotlin.auth.config.AuthorizationServiceClone
import com.kt.altitudekotlin.auth.config.Configuration
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.browser.AnyBrowserMatcher
import net.openid.appauth.browser.BrowserMatcher
import java.io.Serializable
import java.lang.Exception

class AuthConfig: Serializable {
    companion object{
        private var browserMatcher: BrowserMatcher = AnyBrowserMatcher.INSTANCE
        @JvmStatic
        fun createAuthServiceClone(context: Context) : AuthorizationServiceClone? {
            var authService:AuthorizationServiceClone?
            try{
                var builder: AppAuthConfiguration.Builder = AppAuthConfiguration.Builder()
                builder.setBrowserMatcher(browserMatcher)
                builder.setConnectionBuilder(Configuration.getInstance(context).getConnectionBuilder())
                authService = AuthorizationServiceClone(context,builder.build())
                return authService
            }catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
    }
}