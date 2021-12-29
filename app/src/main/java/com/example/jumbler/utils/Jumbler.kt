package com.example.jumbler.utils

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build

class Jumbler : Application() {
    private var currentUuid: String = ""
    private var isOfflineMode: Boolean = false

    fun getCurrentUuid(): String {
        return currentUuid
    }

    fun setCurrentUuid(currentUuid: String) {
        this.currentUuid = currentUuid
    }

    fun getIsOfflineMode(): Boolean {
        return isOfflineMode
    }

    fun setIsOfflineMode(isOfflineMode: Boolean) {
        this.isOfflineMode = isOfflineMode
    }

    fun isDeviceOnline(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network: Network = cm.activeNetwork ?: return false
            val activeNetwork = cm.getNetworkCapabilities(network) ?: return false
            return (activeNetwork.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            ) || activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    || activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH))
        } else {
            return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
        }
    }
}