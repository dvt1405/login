package com.kt.altitudekotlin.auth

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.core.util.Preconditions
import net.openid.appauth.connectivity.ConnectionBuilder
import java.lang.reflect.Array
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class ConnectionBuilderForTesting private constructor() : ConnectionBuilder {
    companion object {
        val INSTANCE: ConnectionBuilderForTesting = ConnectionBuilderForTesting()
        var  TRUSTING_CONTEXT: SSLContext?
        const val TAG = "ConnBuilder"
        const val HTTP = "http"
        const val HTTPS = "https"

        val CONNECTION_TIMEOUT_MS: Int = TimeUnit.SECONDS.toMillis((15)).toInt()
        val READ_TIMEOUT_MS: Int = TimeUnit.SECONDS.toMillis(10).toInt()

        @SuppressLint("TrustAllX509TrustManager")
        @JvmField
        var ANY_CERT_MANAGER = arrayOf<TrustManager>(
            object :
                X509TrustManager {
                override fun getAcceptedIssuers(): kotlin.Array<X509Certificate>? {
                    return null
                }

                override fun checkClientTrusted(
                    certs: kotlin.Array<X509Certificate>,
                    authType: String
                ) {
                }

                override fun checkServerTrusted(
                    certs: kotlin.Array<X509Certificate>,
                    authType: String
                ) {
                }
            })
        @SuppressLint("BadHostnameVerifier")
        private val ANY_HOSTNAME_VERIFIER: HostnameVerifier = HostnameVerifier({
            hostname, session ->  true
        })

        init {
            var context: SSLContext?
            try {
                context = SSLContext.getInstance("SSL")
            } catch (e: NoSuchAlgorithmException) {
                context = null
            }
            var initContext: SSLContext? = null
            if (context != null) {
                try {
                    context.init(null, ANY_CERT_MANAGER, SecureRandom())
                    initContext = context

                } catch (e: KeyManagementException) {
                    Log.e("", "Failed to initialize trusting SSL context")
                }
            }
            TRUSTING_CONTEXT = initContext
        }
    }

    override fun openConnection(uri: Uri): HttpURLConnection {
        Preconditions.checkNotNull(uri, "Url must be not null")
        Preconditions.checkArgument(
            HTTP == uri.scheme || HTTPS == uri.scheme,
            "scheme or uri must be http or https")
        var conn: HttpURLConnection = URL(uri.toString()).openConnection() as HttpURLConnection
        conn.connectTimeout = CONNECTION_TIMEOUT_MS
        conn.readTimeout = READ_TIMEOUT_MS
        conn.instanceFollowRedirects = false

        if(conn is HttpsURLConnection && TRUSTING_CONTEXT != null) {
            var httpsConn:HttpsURLConnection = conn
            httpsConn.sslSocketFactory = TRUSTING_CONTEXT!!.socketFactory
            httpsConn.hostnameVerifier = ANY_HOSTNAME_VERIFIER
        }
        return conn
    }

}