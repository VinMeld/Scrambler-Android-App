package com.example.jumbler

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.example.jumbler.utils.Jumbler
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default

class SettingsActivity : AppCompatActivity() {
    private val TAG = "SettingsActivity"
    private val preferences: SharedPreferences by lazy {
        getSharedPreferences(
            getString(R.string.app_preference_file_key),
            MODE_PRIVATE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val scopeFirebaseAdd = CoroutineScope(CoroutineName("scopeFirebaseAdd"))
        scopeFirebaseAdd.launch(Default) {
            fun signOut() {
                preferences.edit().putString("username", null).apply()
                preferences.edit().putString("email", null).apply()
                preferences.edit().putString("password", null).apply()
                preferences.edit().putString("remember", "false").apply()
                FirebaseAuth.getInstance().signOut()
            }

            runOnUiThread {
                findViewById<TextView>(R.id.settingsUserName).text =
                    getString(
                        R.string.welcome_user,
                        preferences.getString("username", "").toString()
                    )
                findViewById<TextView>(R.id.textEmailAddress).text =
                    preferences.getString("email", "").toString()
                displayTheme()
            }

            val coroutineDeleteStuff = CoroutineScope(CoroutineName("Delete everything"))
            findViewById<Button>(R.id.buttonDeleteAccount).setOnClickListener {
                if (!(this@SettingsActivity.application as Jumbler).isDeviceOnline()) {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.delete_account_no_internet),
                        Snackbar.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                val dialogBuilder = AlertDialog.Builder(this@SettingsActivity)
                dialogBuilder.setMessage(getString(R.string.delete_account_warning))
                    .setTitle(R.string.delete_account)
                    .setIcon(R.drawable.ic_baseline_warning_24)
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setNegativeButton(getString(R.string.delete_account)) { dialog, _ ->
                        coroutineDeleteStuff.launch(Default) {
                            val userID: String = (this@SettingsActivity.application as Jumbler).getCurrentUuid()

                            // Realtime
                            launch {
                                FirebaseDatabase.getInstance().getReference("Users").child(userID)
                                    .removeValue()
                            }
                            // Auth
                            launch {
                                val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
                                user?.delete()?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d(TAG, "User account deleted.")
                                    } else {
                                        Log.d(TAG, "Failed to delete user")
                                    }
                                }
                            }
                            // Firestore
                            launch {
                                val db: FirebaseFirestore = FirebaseFirestore.getInstance()
                                userID.let { it1 ->
                                    db.collection("Users").document(it1)
                                        .delete()
                                        .addOnSuccessListener {
                                            Log.d(TAG, "DocumentSnapshot successfully deleted!")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w(TAG, "Error deleting document", e)
                                        }
                                }
                            }
                            signOut()
                            dialog.dismiss()
                            finish()
                            startActivity(Intent(this@SettingsActivity, MainActivity::class.java))
                        }
                    }
                val alert: AlertDialog = dialogBuilder.create()
                alert.show()
                alert.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(this@SettingsActivity, R.color.red))
            }

            findViewById<Button>(R.id.signOut).setOnClickListener {
                signOut()
                finish()
                startActivity(Intent(this@SettingsActivity, MainActivity::class.java))
            }

            findViewById<Button>(R.id.buttonMenuMain).setOnClickListener {
                startActivity(Intent(this@SettingsActivity, MenuActivity::class.java))
            }
        }
    }

    private fun displayTheme() {
        val displayModeSpinner: Spinner = findViewById(R.id.displayModeSpinner)
        ArrayAdapter.createFromResource(
            this@SettingsActivity,
            R.array.display_modes,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            displayModeSpinner.adapter = adapter
            val spinnerValue: Int = when {
                preferences.getString("displayMode", "system") == "light" -> {
                    1
                }
                preferences.getString("displayMode", "system") == "dark" -> {
                    2
                }
                else -> {
                    0
                }
            }
            displayModeSpinner.setSelection(spinnerValue)
        }
        displayModeSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when (displayModeSpinner.selectedItemPosition) {
                    0 -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        preferences.edit().putString("displayMode", "system").apply()
                    }
                    1 -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        preferences.edit().putString("displayMode", "light").apply()
                    }
                    else -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        preferences.edit().putString("displayMode", "dark").apply()
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                when {
                    preferences.getString(
                        "displayMode",
                        "system"
                    ) == "light" -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        displayModeSpinner.setSelection(1)
                    }
                    preferences.getString("displayMode", "system") == "dark" -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        displayModeSpinner.setSelection(2)
                    }
                    else -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        displayModeSpinner.setSelection(0)
                    }
                }
            }
        }
    }
}