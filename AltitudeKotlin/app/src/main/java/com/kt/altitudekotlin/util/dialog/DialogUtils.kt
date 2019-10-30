package com.kt.altitudekotlin.util.dialog

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.kt.altitudekotlin.R
import com.kt.altitudekotlin.databinding.DialogConfirmBinding
import com.kt.altitudekotlin.databinding.DialogProgressBinding
import java.util.zip.Inflater

class DialogUtils {
    companion object {
        @JvmStatic
        fun showProgressDialog(context: Context, cancel: Boolean): AlertDialog {
            var binding: DialogProgressBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_progress,
                null,
                false
            )
            var progress: AlertDialog = AlertDialog.Builder(context, R.style.theme_dialog)
                .setView(binding.root)
                .setCancelable(true)
                .create()
            var window: Window? = progress.window
            if (window != null) {
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            progress.show()
            return progress
        }

        @JvmStatic
        fun confirmDiaglog(
            context: Context,
            messages: String,
            buttonOK: String,
            buttonCancel: String): AlertDialog {
            var binding: DialogConfirmBinding = DataBindingUtil.inflate(
                LayoutInflater.from(
                    context
                ),
                R.layout.dialog_confirm,
                null,
                false
            )
            var confirm: AlertDialog =
                AlertDialog.Builder(context, R.style.theme_dialog).setView(binding.root)
                    .setCancelable(true).create()
            binding.messeages = messages
            binding.btnOK.text = buttonOK
            binding.btnCancel.text = buttonCancel
            var window: Window? = confirm.window
            if (window != null) {
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            return confirm
        }
        interface onClickListener {
            fun onClick()
        }
    }
}