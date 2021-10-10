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
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress

class ProfileActivity : AppCompatActivity() {
    private val TAG = "ProfileActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val activityView: RelativeLayout = findViewById(R.id.settingsView)
        val deleteAccountButton: Button = findViewById(R.id.buttonDeleteAccount)
        val progressBar: ProgressBar = findViewById(R.id.userProfileProgressBar)
        val userNameText: TextView = findViewById(R.id.settingsUserName)
        val emailTextView: TextView = findViewById(R.id.textEmailAddress)
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        val userID: String = (this.application as Jumbler).getCurrentUser()
        val scopeFirebaseAdd = CoroutineScope(CoroutineName("scopeFirebaseAdd"))
        val logout = findViewById<Button>(R.id.signOut)
        val menu = findViewById<Button>(R.id.buttonMenuMain)
        scopeFirebaseAdd.launch(Dispatchers.Default) {
            var isOnline = false
            try {
                Log.e(TAG, "profile")
                val timeoutMs = 1500
                val sock = Socket()
                val sockaddr: SocketAddress = InetSocketAddress("8.8.8.8", 53)
                sock.connect(sockaddr, timeoutMs)
                sock.close()
                Log.e(TAG, "wifi")
                isOnline = true
                if (userID == "") {
                    isOnline = false
                }
            } catch (e: IOException) {
                Log.e(TAG, "offline")
                displayTheme()
                Log.e(TAG, "offline")
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    activityView.visibility = View.VISIBLE
                    logout.visibility = View.INVISIBLE
                    emailTextView.text =
                        "Connect to wifi to view more!\n If connected, try restarting!"
                }
                menu.setOnClickListener {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this@ProfileActivity, MenuActivity::class.java))
                }
            }
            delay(500L)
            if (isOnline) {
                fun clearUser() {
                    val preferences: SharedPreferences =
                        getSharedPreferences("checkbox", MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = preferences.edit()
                    editor.putString("remember", "false")
                    editor.apply()
                    val preferencesEmail: SharedPreferences =
                        getSharedPreferences("email", MODE_PRIVATE)
                    val editorEmail: SharedPreferences.Editor = preferencesEmail.edit()
                    editorEmail.putString("email", null)
                    editorEmail.apply()
                    val preferencesPassword: SharedPreferences =
                        getSharedPreferences("email", MODE_PRIVATE)
                    val editorPassword: SharedPreferences.Editor = preferencesPassword.edit()
                    editorPassword.putString("password", null)
                    editorPassword.apply()
                    FirebaseAuth.getInstance().signOut()
                }
                reference.child(userID).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userProfile = snapshot.getValue(User::class.java)
                        if (userProfile != null) {
                            runOnUiThread {
                                userNameText.text =
                                    getString(R.string.welcome_user, userProfile.username)
                                emailTextView.text = userProfile.email
                                progressBar.visibility = View.GONE
                                activityView.visibility = View.VISIBLE
                                deleteAccountButton.visibility = View.VISIBLE
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            getString(R.string.generic_error),
                            Snackbar.LENGTH_LONG
                        ).show()
                        runOnUiThread {
                            progressBar.visibility = View.GONE
                            activityView.visibility = View.VISIBLE
                        }

                    }
                })

                displayTheme()

                val coroutineDeleteStuff = CoroutineScope(CoroutineName("Delete everything"))

                deleteAccountButton.setOnClickListener {
                    val dialogBuilder = AlertDialog.Builder(this@ProfileActivity)
                    dialogBuilder.setMessage(getString(R.string.delete_account_warning))
                        .setTitle(R.string.delete_account)
                        .setIcon(R.drawable.ic_baseline_warning_24)
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.cancel)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setNegativeButton(getString(R.string.delete_account)) { dialog, _ ->
                            coroutineDeleteStuff.launch(Default) {
                                // Realtime
                                launch {
                                    reference.child(userID).removeValue()
                                }
                                // Auth
                                launch {
                                    val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
                                    // Get auth credentials from the user for re-authentication. The example below shows
                                    // email and password credentials but there are multiple possible providers,
                                    // such as GoogleAuthProvider or FacebookAuthProvider.
                                    val credential: AuthCredential = EmailAuthProvider
                                        .getCredential("user@example.com", "password1234")

                                    // Prompt the user to re-provide their sign-in credentials
                                    user?.reauthenticate(credential)
                                        ?.addOnCompleteListener {
                                            Log.d(
                                                TAG,
                                                "User re-authenticated."
                                            )
                                        }
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
                                                Log.d(
                                                    TAG,
                                                    "DocumentSnapshot successfully deleted!"
                                                )
                                            }
                                            .addOnFailureListener { e ->
                                                Log.w(
                                                    TAG,
                                                    "Error deleting document",
                                                    e
                                                )
                                            }
                                    }
                                }
                                clearUser()
                                dialog.dismiss()
                                finish()
                                startActivity(
                                    Intent(
                                        this@ProfileActivity,
                                        MainActivity::class.java
                                    )
                                )
                            }
                        }
                    val alert: AlertDialog = dialogBuilder.create()
                    alert.show()
                    alert.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(ContextCompat.getColor(this@ProfileActivity, R.color.red))
                }

                logout.setOnClickListener {
                    clearUser()
                    finish()
                    startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
                }

                menu.setOnClickListener {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this@ProfileActivity, MenuActivity::class.java))
                }
            }
        }
    }

    private fun CoroutineScope.displayTheme() {
        val displayModeSpinner: Spinner = findViewById(R.id.displayModeSpinner)
        val displayModePreferences = getSharedPreferences("displayMode", MODE_PRIVATE)
        ArrayAdapter.createFromResource(
            this@ProfileActivity,
            R.array.display_modes,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            displayModeSpinner.adapter = adapter
            val spinnerValue: Int = when {
                displayModePreferences.getString("displayMode", "system") == "light" -> {
                    1
                }
                displayModePreferences.getString("displayMode", "system") == "dark" -> {
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
                        displayModePreferences.edit().putString("displayMode", "system")
                            .apply()
                    }
                    1 -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        displayModePreferences.edit().putString("displayMode", "light")
                            .apply()
                    }
                    else -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        displayModePreferences.edit().putString("displayMode", "dark")
                            .apply()
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                when {
                    displayModePreferences.getString(
                        "displayMode",
                        "system"
                    ) == "light" -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        displayModeSpinner.setSelection(1)
                    }
                    displayModePreferences.getString("displayMode", "system") == "dark" -> {
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