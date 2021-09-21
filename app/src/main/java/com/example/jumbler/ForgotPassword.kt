package com.example.jumbler

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        val resetPasswordButton: Button = findViewById(R.id.resetPassword)
        resetPasswordButton.setOnClickListener { resetPassword() }
    }

    private fun resetPassword() {
        val emailEditText: EditText = findViewById(R.id.email)
        val email: String = emailEditText.text.toString().trim { it <= ' ' }

        if (email.isEmpty()) {
            emailEditText.error = getString(R.string.reset_password_empty_email)
            emailEditText.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = getString(R.string.invalid_email)
            emailEditText.requestFocus()
            return
        }

        val dimBackground: LinearLayout = findViewById(R.id.dimBackground)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            findViewById<View>(R.id.forgotPasswordActivity).setRenderEffect(
                RenderEffect.createBlurEffect(
                    16F,
                    16F,
                    Shader.TileMode.MIRROR
                )
            )
        } else {
            dimBackground.visibility = View.VISIBLE
        }

        val progressBar: ProgressBar = findViewById(R.id.progressBar2)
        progressBar.visibility = View.VISIBLE

        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.reset_password_check_email),
                    Snackbar.LENGTH_LONG
                ).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(
                        Intent(
                            this@ForgotPassword,
                            MainActivity::class.java
                        )
                    )
                }, 2750)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                findViewById<View>(R.id.forgotPasswordActivity).setRenderEffect(null)
            } else {
                dimBackground.visibility = View.GONE
            }
            progressBar.visibility = View.GONE
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