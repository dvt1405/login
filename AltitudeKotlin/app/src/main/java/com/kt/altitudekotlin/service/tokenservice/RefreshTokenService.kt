package com.kt.altitudekotlin.service.tokenservice

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import androidx.annotation.RequiresApi
import com.kt.altitudekotlin.App
import com.kt.altitudekotlin.auth.config.AuthStateManager
import com.kt.altitudekotlin.auth.config.AuthorizationServiceClone
import com.kt.altitudekotlin.auth.config.TokenManager
import net.openid.appauth.AuthorizationException
import net.openid.appauth.TokenResponse

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class RefreshTokenService : JobService() {
    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        refreshToken()

        return false
    }

    private fun refreshToken() {
        TokenManager.getInstance(App.get())
            .refreshToken(object : AuthorizationServiceClone.TokenResponseCallback {
                override fun onTokenRequestCompleted(
                    response: TokenResponse?,
                    ex: AuthorizationException?
                ) {
                    if (response != null) {
                        AuthStateManager.getInstance(App.get())
                            .updateAfterTokenResponse(response, ex)
                    } else {
                        AuthStateManager.getInstance(App.get()).scheduleRefreshToken()
                    }
                }

                override fun onTokenRefreshCompleted(
                    accessToken: String?,
                    idToken: String?,
                    ex: AuthorizationException?
                ) {

                }
            })
    }
}