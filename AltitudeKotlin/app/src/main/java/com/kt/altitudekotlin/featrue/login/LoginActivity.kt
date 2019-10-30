package com.kt.altitudekotlin.featrue.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kt.altitudekotlin.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportFragmentManager.beginTransaction().replace(R.id.frame,
            LoginFragment()
        ).commit()
    }

}
