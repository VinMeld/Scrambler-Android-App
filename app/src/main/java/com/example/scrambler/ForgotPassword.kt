package com.example.scrambler

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {
    private var emailEditText: EditText? = null
    private var resetPasswordButton: Button? = null
    private var progessBar: ProgressBar? = null
    var auth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        emailEditText = findViewById(R.id.email)
        resetPasswordButton = findViewById(R.id.resetPassword)
        progessBar = findViewById(R.id.progressBar2)
        auth = FirebaseAuth.getInstance()
        resetPasswordButton?.setOnClickListener(View.OnClickListener { resetPassword() })
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
        progessBar!!.visibility = View.VISIBLE
        auth!!.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    this@ForgotPassword,
                    getString(R.string.reset_password_check_email),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this@ForgotPassword,
                    getString(R.string.generic_error),
                    Toast.LENGTH_LONG
                ).show()
            }
            progessBar!!.visibility = View.GONE
        }
    }
}