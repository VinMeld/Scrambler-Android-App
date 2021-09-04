package com.example.scrambler

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.scrambler.Utils.Scrambler
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var forgotPassword: TextView? = null
    private var editTextEmail: EditText? = null
    private var editTextPassword: EditText? = null
    private var signIn: Button? = null
    private var mAuth: FirebaseAuth? = null
    private var progressBar: ProgressBar? = null
    private var remember: CheckBox? = null
    private var register: TextView? = null
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
                if (user != null) {
                    (this.application as Scrambler).setCurrentUser(user.uid)
                }
                if (user!!.isEmailVerified) {
                    val preferencesEmail = getSharedPreferences("email", MODE_PRIVATE)
                    val editorEmail = preferencesEmail.edit()
                    editorEmail.putString("email", email)
                    editorEmail.apply()
                    val preferencesPassword = getSharedPreferences("password", MODE_PRIVATE)
                    val editorPassword = preferencesPassword.edit()
                    editorPassword.putString("password", password)
                    editorPassword.apply()
                    startActivity(Intent(this@MainActivity, MenuActivity::class.java))
                } else {
                    user.sendEmailVerification()
                    Toast.makeText(
                        this@MainActivity,
                        "Check your email to verify your account!",
                        Toast.LENGTH_LONG
                    ).show()
                    progressBar!!.visibility = View.GONE
                }
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Failed to login please. Please check your credentials",
                    Toast.LENGTH_SHORT
                ).show()
                progressBar!!.visibility = View.GONE
            }
        }
    }

    override fun onPause() {
        progressBar!!.visibility = View.GONE
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        register = findViewById(R.id.textRegister)
        register?.setOnClickListener(this)
        signIn = findViewById(R.id.buttonLogin)
        signIn?.setOnClickListener(this)
        editTextEmail = findViewById(R.id.textEmail)
        editTextPassword = findViewById(R.id.textPassword)
        progressBar = findViewById(R.id.progressBar)
        mAuth = FirebaseAuth.getInstance()
        forgotPassword = findViewById(R.id.textForgot)
        forgotPassword?.setOnClickListener(this)
        remember = findViewById(R.id.checkBox)

        val preferences = getSharedPreferences("checkbox", MODE_PRIVATE)
        val checkbox = preferences.getString("remember", "")
        val preferencesEmail = getSharedPreferences("email", MODE_PRIVATE)
        val email = preferencesEmail.getString("email", "").toString()
        val preferencesPassword = getSharedPreferences("password", MODE_PRIVATE)
        val password = preferencesPassword.getString("password", "").toString()

        if (checkbox.equals("true") && email != "" && password != "") {
            mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        (this.application as Scrambler).setCurrentUser(user.uid)
                        startActivity(Intent(this@MainActivity, MenuActivity::class.java))
                    }
                }
            }
        } else if (checkbox.equals("false")) {
            Toast.makeText(this, "Please Sign In", Toast.LENGTH_SHORT).show()
        }
        remember?.setOnCheckedChangeListener { buttonView, _ ->
            if (buttonView.isChecked) {
                getSharedPreferences("checkbox", MODE_PRIVATE).edit().putString("remember", "true")
                    .apply()
                Toast.makeText(this, "Remember Me Checked", Toast.LENGTH_SHORT).show()
            } else if (!buttonView.isChecked) {
                val editor = getSharedPreferences("checkbox", MODE_PRIVATE).edit()
                editor.putString("remember", "false")
                editor.apply()
                Toast.makeText(this, "Remember Me Unchecked", Toast.LENGTH_SHORT).show()
            }
        }
    }
}