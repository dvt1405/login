package com.kt.altitudekotlin.featrue.startapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.kt.altitudekotlin.App
import com.kt.altitudekotlin.MainActivity
import com.kt.altitudekotlin.R
import com.kt.altitudekotlin.featrue.login.LoginActivity

class StartAppActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_app)
        handleLogin()
    }
    fun startLogin() {
        if (!App.get().isSignIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    fun handleLogin() {
        Handler().postDelayed(
            {
                if (App.get().isSignIn()) {
                    MainActivity.startFromThis(this)
                } else {
                    startLogin()
                }
            }, 1000
        )
    }
}
