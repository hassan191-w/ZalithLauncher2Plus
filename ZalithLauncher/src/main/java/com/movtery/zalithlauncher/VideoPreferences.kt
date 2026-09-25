package com.movtery.zalithlauncher

import android.content.Context

object VideoPreferences {
    private const val PREFS_NAME = "launcher_video_prefs"
    private const val KEY_VIDEO_URI = "splash_video_uri"

    fun saveVideoUri(context: Context, uri: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_VIDEO_URI, uri).apply()
    }

    fun getVideoUri(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_VIDEO_URI, null)
    }

    fun clearVideoUri(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_VIDEO_URI).apply()
    }
}
