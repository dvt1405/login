package com.kt.altitudekotlin.auth.config

import android.content.Context
import com.kt.altitudekotlin.auth.AuthConfig
import com.kt.altitudekotlin.model.AppConfigModel
import com.kt.altitudekotlin.util.dialog.DialogUtils
import net.openid.appauth.*
import java.lang.ref.WeakReference
import java.util.HashMap
import java.util.concurrent.atomic.AtomicReference

class TokenManager private constructor(private val context: Context){
    private var authStateManager: AuthStateManager
    var authorizationServiceClone: AuthorizationServiceClone?
    init {
        authStateManager = AuthStateManager.getInstance(context)
        authorizationServiceClone = AuthConfig.createAuthServiceClone(context)
    }

    companion object{
        private var INSTANCE:AtomicReference<WeakReference<TokenManager>>? = AtomicReference(WeakReference<TokenManager>(null))
        @JvmStatic
        fun getInstance(context: Context): TokenManager {
            var manager: TokenManager? = INSTANCE?.get()?.get()
            if (manager == null) {
                manager = TokenManager(context)
                INSTANCE!!.set(WeakReference(manager))
            }
            return manager
        }
    }

    fun exchangeAuthorizationCode(
        context: Context,
        authorizationResponse: AuthorizationResponse,
        callback: AuthorizationServiceClone.TokenResponseCallback
    ) {
        val progressDialog = DialogUtils.showProgressDialog(context, false)
        val map = HashMap<String, String>()
        map["client_secret"] = AppConfigModel.getInstance(context)!!.auth.ClientSecret
        performTokenRequest(
            authorizationResponse.createTokenExchangeRequest(map),
            object : AuthorizationServiceClone.TokenResponseCallback {
                override fun onTokenRequestCompleted(response: TokenResponse?, ex: AuthorizationException?) {
                    callback.onTokenRequestCompleted(response, ex)
                        progressDialog.dismiss()
                }

                override fun onTokenRefreshCompleted(
                    accessToken: String?,
                    idToken: String?,
                    ex: AuthorizationException?
                ) {

                }
            })
    }
    fun refreshToken(callback: AuthorizationServiceClone.TokenResponseCallback) {
        try {
            val map = HashMap<String, String?>()
            map["client_secret"] = AppConfigModel.getInstance(context)?.auth?.ClientSecret
            performTokenRequest(
                authStateManager.current?.createTokenRefreshRequest(map), callback
            )
        } catch (e: IllegalStateException) {
            callback.onTokenRequestCompleted(null, null)
        }

    }


    private fun performTokenRequest(
        request: TokenRequest?,
        callback: AuthorizationServiceClone.TokenResponseCallback
    ) {
        val clientAuthentication: ClientAuthentication
        try {
            clientAuthentication = authStateManager.current!!.getClientAuthentication()
        } catch (ex: ClientAuthentication.UnsupportedAuthenticationMethod) {
            return
        }

        authorizationServiceClone?.performTokenRequest(request!!, clientAuthentication, callback)
    }
    fun getAuthService(): AuthorizationServiceClone? {
        return authorizationServiceClone
    }

}
