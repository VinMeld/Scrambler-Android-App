package com.example.scrambler

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.scrambler.R
import android.content.Intent
import com.example.scrambler.RegisterUser
import com.example.scrambler.ForgotPassword
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.example.scrambler.MenuActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var forgotPassword: TextView? = null
    private var editTextEmail: EditText? = null
    private var editTextPassword: EditText? = null
    private var signIn: Button? = null
    private var mAuth: FirebaseAuth? = null
    private var progressBar: ProgressBar? = null
    private var remember: CheckBox? = null
    override fun onClick(v: View) {
        when (v.id) {
            R.id.textRegister -> startActivity(Intent(this, RegisterUser::class.java))
            R.id.buttonLogin -> userLogin()
            R.id.textForgot -> startActivity(Intent(this, ForgotPassword::class.java))
        }
    }

    private fun userLogin() {
        val email = editTextEmail!!.text.toString().trim { it <= ' ' }
        val password = editTextPassword!!.text.toString().trim { it <= ' ' }
        if (email.isEmpty()) {
            editTextEmail!!.error = "Email required!"
            editTextEmail!!.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail!!.error = "Please provide a valid email!"
            editTextEmail!!.requestFocus()
            return
        }
        if (password.isEmpty()) {
            editTextPassword!!.error = "Password required!"
            editTextPassword!!.requestFocus()
            return
        }
        if (password.length < 6) {
            editTextPassword!!.error = "Min password is 6 characters!"
            editTextPassword!!.requestFocus()
            return
        }
        progressBar!!.visibility = View.VISIBLE
        mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user!!.isEmailVerified) {
                    startActivity(Intent(this@MainActivity, MenuActivity::class.java))
                } else {
                    user.sendEmailVerification()
                    Toast.makeText(this@MainActivity, "Check your email to verify your account!", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this@MainActivity, "Failed to login please. Please check your credentials", Toast.LENGTH_SHORT).show()
                progressBar!!.visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val register = findViewById<TextView>(R.id.textRegister)
        register.setOnClickListener(this)
        signIn = findViewById(R.id.buttonLogin)
        signIn?.setOnClickListener(this)
        editTextEmail = findViewById(R.id.textEmail)
        editTextPassword = findViewById(R.id.textPassword)
        progressBar = findViewById(R.id.progressBar)
        mAuth = FirebaseAuth.getInstance()
        forgotPassword = findViewById(R.id.textForgot)
        forgotPassword?.setOnClickListener(this)
        remember = findViewById(R.id.checkBox)
        editTextEmail?.setText("vinaymeldrum@gmail.com")
        editTextPassword?.setText("vinay123")
    }
}