package com.example.scrambler

import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.EditText
import android.widget.ProgressBar
import com.google.firebase.auth.FirebaseAuth
import android.os.Bundle
import com.example.scrambler.R
import android.content.Intent
import android.util.Patterns
import android.view.View
import com.example.scrambler.MainActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.database.FirebaseDatabase
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser

class RegisterUser : AppCompatActivity(), View.OnClickListener {
    private var banner: TextView? = null
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
        banner = findViewById(R.id.textScramble)
        banner?.setOnClickListener(this)
        registerUser = findViewById(R.id.buttonRegisterUser)
        registerUser?.setOnClickListener(this)
        editTUsername = findViewById(R.id.textUsername)
        editTextEmail = findViewById(R.id.textEmail)
        editTextPassword = findViewById(R.id.textPassword)
        progressBar = findViewById(R.id.progressBar)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.textScramble -> startActivity(Intent(this, MainActivity::class.java))
            R.id.buttonRegisterUser -> registerUser()
        }
    }

    private fun registerUser() {
        val email = editTextEmail!!.text.toString().trim { it <= ' ' }
        val username = editTUsername!!.text.toString().trim { it <= ' ' }
        val password = editTextPassword!!.text.toString().trim { it <= ' ' }
        if (username.isEmpty()) {
            editTUsername!!.error = "Username required!"
            editTUsername!!.requestFocus()
            return
        }
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
        mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        println("$username $email")
                        val user = User(username, email)
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                                .setValue(user).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this@RegisterUser, "User has been registered successfully!", Toast.LENGTH_LONG).show()
                                        val firebaseUser = FirebaseAuth.getInstance().currentUser
                                        val userID = firebaseUser!!.uid
                                        user.setUuid(userID)
                                    } else {
                                        Toast.makeText(this@RegisterUser, "Failed to register! Try again!", Toast.LENGTH_LONG).show()
                                    }
                                    progressBar!!.visibility = View.GONE
                                }
                    } else {
                        Toast.makeText(this@RegisterUser, "Failed to register! Try again!", Toast.LENGTH_LONG).show()
                        progressBar!!.visibility = View.GONE
                    }
                }
    }
}