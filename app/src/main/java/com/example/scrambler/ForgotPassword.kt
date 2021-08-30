package com.example.scrambler

import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.ProgressBar
import com.google.firebase.auth.FirebaseAuth
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import com.example.scrambler.R
import com.google.android.gms.tasks.OnCompleteListener
import android.widget.Toast

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
            emailEditText!!.error = "Email required!"
            emailEditText!!.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText!!.error = "Please provide a valid email!"
            emailEditText!!.requestFocus()
            return
        }
        progessBar!!.visibility = View.VISIBLE
        auth!!.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@ForgotPassword, "Check your email to reset your password", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@ForgotPassword, "Try again! Something went wrong!", Toast.LENGTH_LONG).show()
            }
            progessBar!!.visibility = View.GONE
        }
    }
}