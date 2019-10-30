package com.kt.altitudekotlin

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.databinding.DataBindingUtil
import com.kt.altitudekotlin.databinding.ActivityMainBinding
import com.kt.altitudekotlin.featrue.login.LoginActivity
import com.kt.altitudekotlin.util.logout.LogoutUtils

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.btnLogout.setOnClickListener{
            LogoutUtils.logout(this)
        }
    }

    companion object {
        fun startFromThis(activity: Activity) {
            activity.startActivity(Intent(activity, MainActivity::class.java))
        }
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
                } else {
                    startLogin()
                }
            }, 1000
        )
    }

}
