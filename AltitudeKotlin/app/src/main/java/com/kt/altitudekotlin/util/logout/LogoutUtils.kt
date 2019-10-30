package com.kt.altitudekotlin.util.logout

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.kt.altitudekotlin.R
import com.kt.altitudekotlin.auth.config.AuthStateManager
import com.kt.altitudekotlin.databinding.DialogConfirmBinding
import com.kt.altitudekotlin.featrue.startapp.StartAppActivity

class LogoutUtils {
    companion object {
        private const val LOG_OUT = "Are you sure to logout?"
        private const val BTN_OK = "OK"
        private const val BTN_CANCEL = "Cancel"
        @JvmStatic
        fun logout(activity: Activity) {
            var logoutDiaglog: AlertDialog = confirmDiaglog(
                activity, LOG_OUT, BTN_OK,
                BTN_CANCEL
            )
        }

        @JvmStatic
        private fun confirmDiaglog(
            activity: Activity,
            messages: String,
            buttonOK: String,
            buttonCancel: String
        ): AlertDialog {
            var dialogBinding = DataBindingUtil.inflate<DialogConfirmBinding>(
                LayoutInflater.from(activity),
                R.layout.dialog_confirm,
                null,
                false
            )
            var confirm = AlertDialog.Builder(activity).setView(dialogBinding.root).create()
            dialogBinding.messeages = messages
            dialogBinding.btnOK.text = buttonOK
            dialogBinding.btnCancel.text = buttonCancel
            dialogBinding.btnOK.setOnClickListener {
                AuthStateManager.getInstance(activity).clear()
                activity.startActivity(Intent(activity,StartAppActivity::class.java))
            }
            dialogBinding.btnCancel.setOnClickListener {
                confirm.dismiss()
            }
            var window: Window? = confirm.window
            if (window != null) {
                window.setBackgroundDrawable(ColorDrawable(Color.alpha(100)))
            }
            confirm.show()
            return confirm
        }
    }
}