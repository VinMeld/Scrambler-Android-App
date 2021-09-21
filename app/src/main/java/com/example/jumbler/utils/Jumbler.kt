package com.example.jumbler.utils

import android.app.Application

class Jumbler : Application() {
    private var currentUuid: String = ""
    private var isOffline: Boolean = false

    fun getCurrentUser(): String {
        return currentUuid
    }

    fun getIsOffline(): Boolean {
        return isOffline
    }

    @JvmName("setSomeVariable1")
    fun setCurrentUser(currentUuid: String) {
        this.currentUuid = currentUuid
    }

    fun setIsOffline(isOffline: Boolean) {
        this.currentUuid = currentUuid
    }
}