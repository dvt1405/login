package com.kt.altitudekotlin.featrue.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.kt.altitudekotlin.App
import com.kt.altitudekotlin.MainActivity
import com.kt.altitudekotlin.R
import com.kt.altitudekotlin.api.APIControl
import com.kt.altitudekotlin.api.request.DeviceRequest
import com.kt.altitudekotlin.api.response.DeviceResponse
import com.kt.altitudekotlin.auth.config.AuthStateManager
import com.kt.altitudekotlin.auth.config.AuthorizationServiceClone
import com.kt.altitudekotlin.auth.config.Configuration
import com.kt.altitudekotlin.auth.config.TokenManager
import com.kt.altitudekotlin.databinding.FragmentLoginBinding
import com.kt.altitudekotlin.model.AppConfigModel
import com.kt.altitudekotlin.model.Auth
import com.kt.altitudekotlin.util.dialog.DialogUtils
import com.kt.altitudekotlin.util.preference.PreferenceUtils
import kotlinx.android.synthetic.main.fragment_login.*
import net.openid.appauth.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginFragment : Fragment() {
    companion object {
        const val RC_AUTH = 100
        const val KEY_CHECK_DRIECT_TO_LOGIN = "KeyCheckDriectToLogin"
    }

    lateinit var binding: FragmentLoginBinding
    var authStateManager: AuthStateManager? = null
    var authRequest: AuthorizationRequest? = null
    private var authServiceClone: AuthorizationServiceClone? = null
    var mAuthServiceClone: AuthorizationServiceClone?
        get() = authServiceClone
        set(value) {
            authServiceClone = value
        }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val activity = activity ?: return
        binding.appConfig = AppConfigModel.getInstance(context!!)
        binding.btnLogin.setOnClickListener {
            requestAuth()
        }

        initAuth()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        return binding.root
    }

    fun checkDirecToLogin() {
        val arguments = arguments
        if (arguments != null) {
            val directToLogin = arguments.getBoolean(KEY_CHECK_DRIECT_TO_LOGIN)
            if (directToLogin) {
                btnLogin.performClick()
            }
        }
    }

    fun initAuth() {
        authStateManager = AuthStateManager.getInstance(context!!)
        recreateAuthorizationService()
    }

    fun requestAuth() {
        var auth: Auth = AppConfigModel.getInstance(context!!)!!.auth
        var progressDialog = DialogUtils.showProgressDialog(context!!, false)

        AuthorizationServiceConfiguration.fetchFromUrl(
            Uri.parse(auth.OpenIdConfigurationUrl),
            AuthorizationServiceConfiguration.RetrieveConfigurationCallback { config: AuthorizationServiceConfiguration?,
                                                                              ex: AuthorizationException? ->
                progressDialog.dismiss()
                if (config == null) {
                    if (ex != null) {

                    }
                } else {
                    authStateManager!!.replace(AuthState(config!!))
                    var builder = AuthorizationRequest.Builder(
                        config,
                        auth.ClientID,
                        ResponseTypeValues.CODE,
                        Uri.parse(auth.RedirectUri)
                    )
                    var otherParams = HashMap<String, String>()
                    otherParams.put("access_type", "offline")
                    otherParams.put("flags", "force_logout")
                    if (auth.TenantID != null) {
                        otherParams.put("tenant_id", auth.TenantID!!)
                    }
                    authRequest = builder
                        .setPrompt(AuthorizationRequest.Prompt.LOGIN)
                        .setClientId(auth.ClientID)
                        .setRedirectUri(Uri.parse(auth.getRedirectUriHttps()))
                        .setScopes(
                            AuthorizationRequest.Scope.OPENID,
                            AuthorizationRequest.Scope.EMAIL,
                            AuthorizationRequest.Scope.PROFILE
                        )
                        .setAdditionalParameters(otherParams)
                        .build()
                    doAuthorization()
                }


            }, Configuration.getInstance(context!!).getConnectionBuilder()
        )
    }


    private fun recreateAuthorizationService() {
        if (mAuthServiceClone != null) {
            mAuthServiceClone!!.dispose()
            mAuthServiceClone = null
        }
        mAuthServiceClone = TokenManager.getInstance(context!!).authorizationServiceClone
        authRequest = null
    }


    private fun doAuthorization() {
        val activity = activity ?: return
        if (authRequest == null) return
        if (authServiceClone == null) return

        val requestAuth = authRequest
        val intentBuilder = authServiceClone!!.createCustomTabsIntentBuilder(requestAuth!!.toUri())
        intentBuilder.setToolbarColor(getActivity()!!.getResources().getColor(R.color.browser_actions_bg_grey))
        intentBuilder.setInstantAppsEnabled(true)

        val authIntent =
            authServiceClone!!.getAuthorizationRequestIntent(requestAuth, intentBuilder.build())
        startActivityForResult(authIntent,
            RC_AUTH
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i("OnactivityResult", "runnings")
        val activity = activity ?: return
        if (requestCode == RC_AUTH) {
            val authResp = AuthorizationResponse.fromIntent(data!!)
            val ex = AuthorizationException.fromIntent(data)
            if (authResp != null || ex != null) {
                authStateManager!!.updateAfterAuthorization(authResp, ex)
            }
            if (authResp != null) {
                TokenManager.getInstance(activity.applicationContext).exchangeAuthorizationCode(
                    activity,
                    authResp,
                    object : AuthorizationServiceClone.TokenResponseCallback {
                        override fun onTokenRequestCompleted(
                            response: TokenResponse?,
                            ex: AuthorizationException?
                        ) {
                            handleCodeExchangeResponse(response, ex)
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
    }

    fun handleCodeExchangeResponse(@Nullable tokenResponse: TokenResponse?, @Nullable authException: AuthorizationException?) {
        if (tokenResponse == null) run {
            val message =
                "Authorization Code exchange failed" + if (authException != null) authException.error else ""
            Log.d("login", message)
        } else {
            authStateManager!!.updateAfterTokenResponse(tokenResponse, authException)
            activity ?: return

            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                val result: InstanceIdResult? = it.result
                if (result == null) return@addOnCompleteListener
                var token = result.token

                APIControl.getInstance().apiProperty.postDevice(DeviceRequest(token))
                    .enqueue(object : Callback<DeviceResponse> {
                        override fun onFailure(call: Call<DeviceResponse>, t: Throwable) {
                        }

                        override fun onResponse(
                            call: Call<DeviceResponse>,
                            response: Response<DeviceResponse>
                        ) {
                            val deviceResponse: DeviceResponse? = response.body() ?: return
                            PreferenceUtils.getInstance(context!!)
                                .saveDeviceId(deviceResponse!!.deviceId!!)
                            MainActivity.startFromThis(activity!!)
                            activity!!.finish()
                        }

                    })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (authServiceClone != null) {
            authServiceClone!!.dispose()
        }
    }


}