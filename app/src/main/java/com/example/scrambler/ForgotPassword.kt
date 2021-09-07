package com.example.scrambler

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class ForgotPassword : AppCompatActivity() {
    private var dimBackground: LinearLayout? = null
    private var emailEditText: EditText? = null
    private var resetPasswordButton: Button? = null
    private var progressBar: ProgressBar? = null
    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        emailEditText = findViewById(R.id.email)
        resetPasswordButton = findViewById(R.id.resetPassword)
        progressBar = findViewById(R.id.progressBar2)
        auth = FirebaseAuth.getInstance()
        resetPasswordButton?.setOnClickListener { resetPassword() }
    }

    private fun resetPassword() {
        val email = emailEditText!!.text.toString().trim { it <= ' ' }
        if (email.isEmpty()) {
            emailEditText!!.error = getString(R.string.reset_password_empty_email)
            emailEditText!!.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText!!.error = getString(R.string.invalid_email)
            emailEditText!!.requestFocus()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            findViewById<View>(R.id.forgotPasswordActivity).setRenderEffect(
                RenderEffect.createBlurEffect(
                    16F,
                    16F,
                    Shader.TileMode.MIRROR
                )
            )
        } else {
            dimBackground = findViewById(R.id.dimBackground)
            dimBackground!!.visibility = View.VISIBLE
        }
        progressBar!!.visibility = View.VISIBLE

        auth!!.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.reset_password_check_email),
                    Snackbar.LENGTH_LONG
                ).show()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                findViewById<View>(R.id.forgotPasswordActivity).setRenderEffect(null)
            } else {
                dimBackground!!.visibility = View.GONE
            }
            progressBar!!.visibility = View.GONE
        }
            .addOnFailureListener { exception ->
                if (exception is FirebaseAuthInvalidUserException) {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.reset_password_error),
                        Snackbar.LENGTH_LONG
                    ).show()
                } else {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.generic_error),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
    }
}