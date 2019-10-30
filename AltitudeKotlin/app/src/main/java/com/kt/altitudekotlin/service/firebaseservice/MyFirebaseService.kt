package com.kt.altitudekotlin.service.firebaseservice

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class MyFirebaseService : FirebaseMessagingService() {
    companion object {
        const val TAG = "Firebase Message"
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        Timber.d(TAG + p0.getFrom()!!)

        // Check if message contains a data payload.
        if (p0.getData().size > 0) {
            Log.d(TAG, "Message data payload: " + p0.getData())
            if (p0.getNotification() != null) {
                Log.d(
                    TAG,
                    "Message Notification Body: " + p0.getNotification()!!.getBody()!!
                )
            }
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.i("Token", p0)
        sendRegistrationToServer(p0)

    }

    private fun sendRegistrationToServer(token: String) {
        Log.i("Sending token to server", "running")

    }
}
