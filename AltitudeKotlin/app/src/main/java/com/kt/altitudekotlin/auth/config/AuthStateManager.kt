package com.kt.altitudekotlin.auth.config

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.AnyThread
import com.auth0.android.jwt.JWT
import com.firebase.jobdispatcher.*
import com.kt.altitudekotlin.App
import com.kt.altitudekotlin.service.tokenservice.RefreshTokenService
import net.openid.appauth.*
import org.json.JSONException
import timber.log.Timber
import java.lang.IllegalStateException
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock

class AuthStateManager private constructor(context: Context) {
    private val mPreference: SharedPreferences
    private val mPreferencesLock: ReentrantLock
    private val mCurrentAuthState: AtomicReference<AuthState>
    init {
        mPreference = context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
        mPreferencesLock = ReentrantLock()
        mCurrentAuthState = AtomicReference(AuthState())
    }
    companion object {

        private val INSTANCE_REF = AtomicReference(WeakReference<AuthStateManager>(null))

        private const val TAG = "AuthStateManager"

        private const val STORE_NAME = "AuthState"

        private const val KEY_STATE = "state"
        @AnyThread
        @JvmStatic
        fun getInstance(context: Context): AuthStateManager {
            var manager = INSTANCE_REF.get().get()
            if (manager == null) {
                manager = AuthStateManager(context.applicationContext)
                INSTANCE_REF.set(WeakReference(manager))
            }

            return manager
        }
    }

    val current: AuthState?
        @AnyThread
        get() {
            if (mCurrentAuthState.get() == null) {
                return mCurrentAuthState.get()
            }
            val state = readState()
            mCurrentAuthState.set(state)
            return mCurrentAuthState.get()
        }



    fun readState(): AuthState {
        mPreferencesLock.lock()
        try {
            val currentState = mPreference.getString(KEY_STATE, null) ?: return AuthState()
            try {
                return AuthState.jsonDeserialize(currentState)
            } catch (ex: JSONException) {
                ex.printStackTrace()
                return AuthState()
            }
        } finally {
            mPreferencesLock.unlock()
        }
    }

    fun writeState(state: AuthState?) {
        mPreferencesLock.lock()
        try {
            val edit = mPreference.edit()
            if (state == null) {
                edit.remove(KEY_STATE)
            } else {
                edit.putString(KEY_STATE, state.jsonSerializeString())
            }
            if (!edit.commit()) {
                throw IllegalStateException("Failed to write state")
            }
        } finally {
            mPreferencesLock.unlock()
        }
    }

    fun replace(state: AuthState?): AuthState? {
        writeState(state)
        mCurrentAuthState.set(state)
        return state
    }

    fun updateAfterAuthorization(
        res: AuthorizationResponse?,
        ex: AuthorizationException?
    ): AuthState? {
        val current = current
        current?.update(res, ex)
        return replace(current)
    }

    fun updateAfterTokenResponse(resToken: TokenResponse?, ex: AuthorizationException?) {
        if (resToken == null && ex == null) return
        val current = this.current
        current!!.update(resToken, ex)

        replace(current)

        scheduleRefreshToken()
    }

    fun resetState(): AuthState? {
        val clearedState = AuthState()
        return replace(clearedState)
    }

    fun updateAfterRegistration(
        res: RegistrationResponse?,
        ex: AuthorizationException?
    ): AuthState? {
        val current = current
        if (ex != null) {
            return current
        }
        current?.update(res)
        return replace(current)
    }

    fun clear() {
        writeState(null)
        mCurrentAuthState.set(null)
        mPreference.edit().clear().apply()
    }

    fun scheduleRefreshToken() {
        val current = this.current
        val accessToken = current!!.getAccessToken()
        if (accessToken != null) {
            val jwt = JWT(accessToken!!)
            Timber.d("Schedule refresh token: start schedule")
            if (jwt.expiresAt == null) return
            var timeRestSecond =
                (jwt.expiresAt!!.time - System.currentTimeMillis()).toFloat() / 1000f * 0.7f
            if (timeRestSecond < 0) timeRestSecond = 0f
            val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(App.get()))
            val myJob = dispatcher.newJobBuilder()
                .setService(RefreshTokenService::class.java as Class<out JobService>)
                .setTag(RefreshTokenService::class.java!!.getSimpleName())
                .setRecurring(false)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(
                    Trigger.executionWindow(
                        timeRestSecond.toInt(),
                        timeRestSecond.toInt() + 60
                    )
                )
                .setReplaceCurrent(false)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .build()

            dispatcher.mustSchedule(myJob)
        }
    }
}