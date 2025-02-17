package com.ximoli.neurafruit.Utils

import android.content.Context
import android.net.ConnectivityManager

object NetworkUtils {
    fun isConnectedToInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }
}
