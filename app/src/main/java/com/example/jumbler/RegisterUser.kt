package com.example.jumbler

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.util.concurrent.Executors

class RegisterUser : AppCompatActivity(), View.OnClickListener {
    private var dimBackground: LinearLayout? = null
    private var registerUser: TextView? = null
    private var editTUsername: EditText? = null
    private var editTextEmail: EditText? = null
    private var editTextPassword: EditText? = null
    private var progressBar: ProgressBar? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)
        mAuth = FirebaseAuth.getInstance()
        registerUser = findViewById(R.id.buttonRegisterUser)
        registerUser?.setOnClickListener(this)
        editTUsername = findViewById(R.id.textUsername)
        editTextEmail = findViewById(R.id.textEmail)
        editTextPassword = findViewById(R.id.textPassword)
        progressBar = findViewById(R.id.progressBar)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonRegisterUser -> registerUser()
        }
    }

    private fun registerUser() {
        var finished = false
        val email = editTextEmail!!.text.toString().trim { it <= ' ' }
        val username = editTUsername!!.text.toString().trim { it <= ' ' }
        val password = editTextPassword!!.text.toString().trim { it <= ' ' }
        if (email.isEmpty()) {
            editTextEmail!!.error = getString(R.string.empty_email)
            editTextEmail!!.requestFocus()
            return
        }
        if (username.isEmpty()) {
            editTUsername!!.error = getString(R.string.empty_username)
            editTUsername!!.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail!!.error = getString(R.string.invalid_email)
            editTextEmail!!.requestFocus()
            return
        }
        if (password.isEmpty()) {
            editTextPassword!!.error = getString(R.string.empty_password)
            editTextPassword!!.requestFocus()
            return
        }
        if (password.length < 6) {
            editTextPassword!!.error = getString(R.string.invalid_password)
            editTextPassword!!.requestFocus()
            return
        }
        val db = FirebaseFirestore.getInstance()
        val user = db.collection("Users")
        val getUser = CoroutineScope(CoroutineName("getUser"))
        getUser.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            Log.e("RegisterUser", "in user information")
            val job1 = launch {
                user.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userSnapShot = task.result
                        var isUnique = true
                        if (userSnapShot != null) {
                            for (i in userSnapShot) {
                                if (i.get("username") as String == username) {
                                    isUnique = false
                                    Log.e(
                                        "Register User",
                                        "It is not unique!! $username + " + i.get("username")
                                    )
                                }
                            }
                        }
                        if (isUnique) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                findViewById<View>(R.id.createUserActivity).setRenderEffect(
                                    RenderEffect.createBlurEffect(16F, 16F, Shader.TileMode.MIRROR)
                                )
                            } else {
                                dimBackground = findViewById(R.id.dimBackground)
                                dimBackground!!.visibility = View.VISIBLE
                            }
                            progressBar!!.visibility = View.VISIBLE
                            mAuth!!.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task1 ->
                                    if (task1.isSuccessful) {
                                        println("$username $email")
                                        val localUser = User(username, email)
                                        FirebaseDatabase.getInstance().getReference("Users")
                                            .child(FirebaseAuth.getInstance().currentUser!!.uid)
                                            .setValue(localUser).addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    val firebaseUser =
                                                        FirebaseAuth.getInstance().currentUser
                                                    val userID = firebaseUser!!.uid
                                                    localUser.addUuid(userID)
                                                    firebaseUser.sendEmailVerification()
                                                    Snackbar.make(
                                                        findViewById(android.R.id.content),
                                                        getString(R.string.new_account_success),
                                                        Snackbar.LENGTH_LONG
                                                    ).show()

                                                    Handler(Looper.getMainLooper()).postDelayed({
                                                        startActivity(
                                                            Intent(
                                                                this@RegisterUser,
                                                                MainActivity::class.java
                                                            )
                                                        )
                                                    }, 2750)

                                                    finished = true
                                                } else {
                                                    Snackbar.make(
                                                        findViewById(android.R.id.content),
                                                        getString(R.string.new_account_fail),
                                                        Snackbar.LENGTH_LONG
                                                    ).show()
                                                    finished = true
                                                }
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                    findViewById<View>(R.id.createUserActivity).setRenderEffect(
                                                        null
                                                    )
                                                } else {
                                                    dimBackground!!.visibility = View.GONE
                                                }
                                                progressBar!!.visibility = View.GONE
                                            }
                                    } else {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            findViewById<View>(R.id.createUserActivity).setRenderEffect(
                                                null
                                            )
                                        } else {
                                            dimBackground!!.visibility = View.GONE
                                        }
                                        progressBar!!.visibility = View.GONE
                                        finished = true
                                    }
                                }.addOnFailureListener { exception ->
                                    if (exception is FirebaseAuthUserCollisionException) {
                                        Snackbar.make(
                                            findViewById(android.R.id.content),
                                            getString(R.string.email_taken),
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                    } else {
                                        Snackbar.make(
                                            findViewById(android.R.id.content),
                                            getString(R.string.generic_error),
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                    }
                                    Log.d("RegisterUser", "get failed with ", exception)
                                }
                        } else {
                            Log.d("RegisterUser", "No such document")
                            Snackbar.make(
                                findViewById(android.R.id.content),
                                getString(R.string.username_taken),
                                Snackbar.LENGTH_LONG
                            ).show()
                            finished = true
                        }
                    }
                }
            }

            while (!job1.isCompleted && !finished) {
                Log.e("ProfileActivity.TAG", "waiting for username to not be null")
                delay(1000L)
            }
            Log.e("RegisterUser", "user 1 is not null : $username")
        }
    }
}
