package com.kt.altitudekotlin.auth.config

import com.kt.altitudekotlin.auth.Utils
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.browser.customtabs.CustomTabsIntent
import net.openid.appauth.*
import net.openid.appauth.browser.BrowserDescriptor
import net.openid.appauth.browser.BrowserSelector
import net.openid.appauth.browser.CustomTabManager
import net.openid.appauth.connectivity.ConnectionBuilder
import net.openid.appauth.internal.Logger
import net.openid.appauth.internal.UriUtil
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URLConnection

class AuthorizationServiceClone(context: Context, clientConfiguration: AppAuthConfiguration) {
    @VisibleForTesting
    internal var mContext: Context? = null

    private var mClientConfiguration: AppAuthConfiguration? = null

    private var mCustomTabManager: CustomTabManager? = null

    private var mBrowser: BrowserDescriptor? = null

    private var mDisposed = false

    var customTabManager: CustomTabManager?
        get() = mCustomTabManager
        set(value) {
            mCustomTabManager = value
        }
    init {
        mContext = context
        mClientConfiguration = clientConfiguration
        mBrowser = BrowserSelector.select(
            context,
            clientConfiguration.browserMatcher
        )
        customTabManager = CustomTabManager(context)
        if(customTabManager != null && mBrowser!!.useCustomTab) {
            customTabManager?.bind(mBrowser!!.packageName)
        }
    }

    @VisibleForTesting
    constructor(
        context: Context,
        clientConfiguration: AppAuthConfiguration,
        browser: BrowserDescriptor?,
        customTabManager: CustomTabManager
    ) : this(context, clientConfiguration) {
        mContext = checkNotNull(context)
        mClientConfiguration = clientConfiguration
        this.customTabManager = customTabManager
        mBrowser = browser
        if (browser != null && browser.useCustomTab) {
            customTabManager.bind(browser.packageName)
        }
    }


    fun createCustomTabsIntentBuilder(vararg possibleUris: Uri): CustomTabsIntent.Builder {
        checkNotDisposed()
        Log.i("CustomtabManager", (this.customTabManager == null).toString())
        return customTabManager!!.createTabBuilder(*possibleUris)
    }

    fun performAuthorizationRequest(
        request: AuthorizationRequest,
        completedIntent: PendingIntent
    ) {
        performAuthorizationRequest(
            request,
            completedIntent,
            null,
            createCustomTabsIntentBuilder().build()
        )
    }

    fun performAuthorizationRequest(
        request: AuthorizationRequest,
        completedIntent: PendingIntent,
        canceledIntent: PendingIntent
    ) {
        performAuthorizationRequest(
            request,
            completedIntent,
            canceledIntent,
            createCustomTabsIntentBuilder().build()
        )
    }

    fun performAuthorizationRequest(
        request: AuthorizationRequest?,
        completedIntent: PendingIntent?,
        canceledIntent: PendingIntent?,
        customTabsIntent: CustomTabsIntent?
    ) {
        checkNotDisposed()
        checkNotNull(request)
        checkNotNull(completedIntent)
        checkNotNull(customTabsIntent)

        val authIntent = prepareAuthorizationRequestIntent(request, customTabsIntent)
        mContext!!.startActivity(
            AuthorizationManagementActivity.createStartIntent(
                mContext,
                request,
                authIntent,
                completedIntent,
                canceledIntent
            )
        )
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun getAuthorizationRequestIntent(
        request: AuthorizationRequest,
        customTabsIntent: CustomTabsIntent
    ): Intent {

        val authIntent = prepareAuthorizationRequestIntent(request, customTabsIntent)
        return AuthorizationManagementActivity.createStartForResultIntent(
            mContext,
            request,
            authIntent
        )
    }

    fun performTokenRequest(
        request: TokenRequest,
        clientAuthentication: ClientAuthentication,
        callback: TokenResponseCallback
    ) {
        checkNotDisposed()
        Logger.debug(
            "Initiating code exchange request to %s",
            request.configuration.tokenEndpoint
        )

        TokenRequestTask(
            request,
            clientAuthentication,
            mClientConfiguration!!.connectionBuilder,
            callback
        )
            .execute()
    }

    fun dispose() {
        if (mDisposed) {
            return
        }else{
            mCustomTabManager!!.dispose()
            mDisposed = true
        }

    }

    private fun checkNotDisposed() {
        check(!mDisposed) { "Service has been disposed and rendered inoperable" }
    }

    private fun prepareAuthorizationRequestIntent(
        request: AuthorizationRequest,
        customTabsIntent: CustomTabsIntent
    ): Intent {
        checkNotDisposed()

        if (mBrowser == null) {
            throw ActivityNotFoundException()
        }

        val requestUri = request.toUri()
        val intent: Intent
        if (mBrowser!!.useCustomTab) {
            intent = customTabsIntent.intent
        } else {
            intent = Intent(Intent.ACTION_VIEW)
        }
        intent.setPackage(mBrowser!!.packageName)
        intent.data = requestUri

        Logger.debug(
            "Using %s as browser for auth, custom tab = %s",
            intent.getPackage(),
            mBrowser!!.useCustomTab.toString()
        )

        Logger.debug(
            "Initiating authorization request to %s",
            request.configuration.authorizationEndpoint
        )

        return intent
    }

    private class TokenRequestTask internal constructor(
        private val mRequest: TokenRequest,
        private val mClientAuthentication: ClientAuthentication,
        private val mConnectionBuilder: ConnectionBuilder,
        private val mCallback: TokenResponseCallback
    ) :
        AsyncTask<Void, Void, JSONObject>() {

        private var mException: AuthorizationException? = null

        override fun doInBackground(vararg voids: Void): JSONObject? {
            var inputStream: InputStream? = null
            try {
                val conn = mConnectionBuilder.openConnection(
                    mRequest.configuration.tokenEndpoint
                )
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                addJsonToAcceptHeader(conn)
                conn.doOutput = true

                val headers = mClientAuthentication
                    .getRequestHeaders(mRequest.clientId)
                if (headers != null) {
                    for ((key, value) in headers) {
                        conn.setRequestProperty(key, value)
                    }
                }

                val parameters = mRequest.requestParameters
                val clientAuthParams = mClientAuthentication
                    .getRequestParameters(mRequest.clientId)
                if (clientAuthParams != null) {
                    parameters.putAll(clientAuthParams)
                }

                val queryData = UriUtil.formUrlEncode(parameters)
                conn.setRequestProperty("Content-Length", queryData.length.toString())
                val wr = OutputStreamWriter(conn.outputStream)

                wr.write(queryData)
                wr.flush()

                if (conn.responseCode >= HttpURLConnection.HTTP_OK && conn.responseCode < HttpURLConnection.HTTP_MULT_CHOICE) {
                    inputStream = conn.inputStream
                } else {
                    inputStream = conn.errorStream
                }
                val response = Utils.readInputStream(inputStream)
                return JSONObject(response)
            } catch (ex: IOException) {
                Logger.debugWithStack(ex, "Failed to complete exchange request")
                mException = AuthorizationException.fromTemplate(
                    AuthorizationException.GeneralErrors.NETWORK_ERROR, ex
                )
            } catch (ex: JSONException) {
                Logger.debugWithStack(ex, "Failed to complete exchange request")
                mException = AuthorizationException.fromTemplate(
                    AuthorizationException.GeneralErrors.JSON_DESERIALIZATION_ERROR, ex
                )
            } catch (ex: NullPointerException) {
                Logger.debugWithStack(ex, "Failed to complete exchange request")
                mException = AuthorizationException.fromTemplate(
                    AuthorizationException.GeneralErrors.NETWORK_ERROR, ex
                )
            } finally {
                Utils.closeQuietly(inputStream)
            }
            return null
        }

        override fun onPostExecute(json: JSONObject) {
            if (mException != null) {
                mCallback.onTokenRequestCompleted(null, mException)
                return
            }

            if (json.has(AuthorizationException.PARAM_ERROR)) {
                var ex: AuthorizationException
                try {
                    val error = json.getString(AuthorizationException.PARAM_ERROR)
                    ex = AuthorizationException.fromOAuthTemplate(
                        AuthorizationException.TokenRequestErrors.byString(error),
                        error,
                        json.optString(AuthorizationException.PARAM_ERROR_DESCRIPTION, null),
                        UriUtil.parseUriIfAvailable(
                            json.optString(AuthorizationException.PARAM_ERROR_URI)
                        )
                    )
                } catch (jsonEx: JSONException) {
                    ex = AuthorizationException.fromTemplate(
                        AuthorizationException.GeneralErrors.JSON_DESERIALIZATION_ERROR,
                        jsonEx
                    )
                }

                mCallback.onTokenRequestCompleted(null, ex)
                return
            }

            val response: TokenResponse
            try {
                response = TokenResponse.Builder(mRequest).fromResponseJson(json).build()
            } catch (jsonEx: JSONException) {
                mCallback.onTokenRequestCompleted(
                    null,
                    AuthorizationException.fromTemplate(
                        AuthorizationException.GeneralErrors.JSON_DESERIALIZATION_ERROR,
                        jsonEx
                    )
                )
                return
            }

            Logger.debug(
                "Token exchange with %s completed",
                mRequest.configuration.tokenEndpoint
            )
            mCallback.onTokenRequestCompleted(response, null)
        }

        private fun addJsonToAcceptHeader(conn: URLConnection) {
            if (TextUtils.isEmpty(conn.getRequestProperty("Accept"))) {
                conn.setRequestProperty("Accept", "application/json")
            }
        }
    }

    interface TokenResponseCallback {
        fun onTokenRequestCompleted(
            response: TokenResponse?,
            ex: AuthorizationException?
        )

        fun onTokenRefreshCompleted(
            accessToken: String?,
            idToken: String?,
            ex: AuthorizationException?
        )
    }

    private class RegistrationRequestTask internal constructor(
        private val mRequest: RegistrationRequest,
        private val mConnectionBuilder: ConnectionBuilder,
        private val mCallback: RegistrationResponseCallback
    ) :
        AsyncTask<Void, Void, JSONObject>() {
        private var mException: AuthorizationException? = null
        override fun doInBackground(vararg voids: Void): JSONObject? {
            var inputStream: InputStream? = null
            val postData = mRequest.toJsonString()
            try {
                val conn = mConnectionBuilder.openConnection(
                    mRequest.configuration.registrationEndpoint!!
                )
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.setRequestProperty("Content-Length", postData.length.toString())
                val wr = OutputStreamWriter(conn.outputStream)
                wr.write(postData)
                wr.flush()

                inputStream = conn.inputStream
                val response = Utils.readInputStream(inputStream)
                return JSONObject(response)
            } catch (ex: IOException) {
                Logger.debugWithStack(ex, "Failed to complete registration request")
                mException = AuthorizationException.fromTemplate(
                    AuthorizationException.GeneralErrors.NETWORK_ERROR, ex
                )
            } catch (ex: JSONException) {
                Logger.debugWithStack(ex, "Failed to complete registration request")
                mException = AuthorizationException.fromTemplate(
                    AuthorizationException.GeneralErrors.JSON_DESERIALIZATION_ERROR, ex
                )
            } catch (ex: NullPointerException) {
                Logger.debugWithStack(ex, "Failed to complete exchange request")
                mException = AuthorizationException.fromTemplate(
                    AuthorizationException.GeneralErrors.NETWORK_ERROR, ex
                )
            } finally {
                Utils.closeQuietly(inputStream)
            }
            return null
        }

        override fun onPostExecute(json: JSONObject) {
            if (mException != null) {
                mCallback.onRegistrationRequestCompleted(null, mException)
                return
            }

            if (json.has(AuthorizationException.PARAM_ERROR)) {
                var ex: AuthorizationException
                try {
                    val error = json.getString(AuthorizationException.PARAM_ERROR)
                    ex = AuthorizationException.fromOAuthTemplate(
                        AuthorizationException.RegistrationRequestErrors.byString(error),
                        error,
                        json.getString(AuthorizationException.PARAM_ERROR_DESCRIPTION),
                        UriUtil.parseUriIfAvailable(
                            json.getString(AuthorizationException.PARAM_ERROR_URI)
                        )
                    )
                } catch (jsonEx: JSONException) {
                    ex = AuthorizationException.fromTemplate(
                        AuthorizationException.GeneralErrors.JSON_DESERIALIZATION_ERROR,
                        jsonEx
                    )
                }

                mCallback.onRegistrationRequestCompleted(null, ex)
                return
            }

            val response: RegistrationResponse
            try {
                response = RegistrationResponse.Builder(mRequest)
                    .fromResponseJson(json).build()
            } catch (jsonEx: JSONException) {
                mCallback.onRegistrationRequestCompleted(
                    null,
                    AuthorizationException.fromTemplate(
                        AuthorizationException.GeneralErrors.JSON_DESERIALIZATION_ERROR,
                        jsonEx
                    )
                )
                return
            } catch (ex: RegistrationResponse.MissingArgumentException) {
                Logger.errorWithStack(ex, "Malformed registration response")
                mException = AuthorizationException.fromTemplate(
                    AuthorizationException.GeneralErrors.INVALID_REGISTRATION_RESPONSE,
                    ex
                )
                return
            }

            Logger.debug(
                "Dynamic registration with %s completed",
                mRequest.configuration.registrationEndpoint
            )
            mCallback.onRegistrationRequestCompleted(response, null)
        }
    }

    interface RegistrationResponseCallback {

        fun onRegistrationRequestCompleted(
            response: RegistrationResponse?,
            ex: AuthorizationException?
        )
    }


}