package com.example.scrambler.Utils
import android.app.Application
import com.example.scrambler.User


class Scrambler : Application() {
    var currentUuid: String? = null
    fun getCurrentUser(): String? {
        return currentUuid
    }

    @JvmName("setSomeVariable1")
    fun setCurrentUser(currentUuid: String?) {
        this.currentUuid = currentUuid
    }
}