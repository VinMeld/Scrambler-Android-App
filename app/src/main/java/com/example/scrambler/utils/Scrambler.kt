package com.example.scrambler.utils

import android.app.Application

class Scrambler : Application() {
    private var currentUuid: String? = null

    fun getCurrentUser(): String? {
        return currentUuid
    }

    @JvmName("setSomeVariable1")
    fun setCurrentUser(currentUuid: String?) {
        this.currentUuid = currentUuid
    }
}