package com.kt.altitudekotlin.util.preference

import android.content.Context
import android.preference.PreferenceManager

class PreferenceUtils private constructor(private val context: Context) {
    private var mContext: Context?
    var mContextP
    get() = mContext
    set(value) {
        mContext = value
    }

    companion object {
        private const val KEY_LAT_LOCATION = "KEY_LAT_LOCATION"
        private const val KEY_LNG_LOCATION = "KEY_LNG_LOCATION"
        const val KEY_GET_TIME_REFRESH_DATA = "KeyGetTimeRefreshData"
        const val KEY_GET_TIME_CHECK_IN_CHOOSE_DATE = "KeyGetTimeCheckInChoiceDate"
        const val KEY_GET_TIME_CHECK_OUT_CHOOSE_DATE = "KeyGetTimeCheckOutChoiceDate"
        private const val KEY_GET_DEVICE_ID = "KeyGetDeviceId"
        private var INSTANCE: PreferenceUtils? = null
        fun getInstance(context: Context):PreferenceUtils {
            if (INSTANCE == null) {
                INSTANCE = PreferenceUtils(context)
            }
            return INSTANCE!!
        }
    }

    init {
        this.mContext = context
    }

    fun saveDeviceId(deviceId: String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        val sharedPreferencesEditor = sharedPreferences.edit()
        sharedPreferencesEditor.putString(KEY_GET_DEVICE_ID, deviceId)
        sharedPreferencesEditor.apply()
    }

    fun getDeviceId(): String? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        return sharedPreferences.getString(KEY_GET_DEVICE_ID, "")
    }
}