package com.example.scrambler

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.scrambler.utils.Scrambler
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var dimBackground: LinearLayout? = null
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
            editTextEmail!!.error = getString(R.string.empty_email)
            editTextEmail!!.requestFocus()
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            findViewById<View>(R.id.loginActivity).setRenderEffect(
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
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.verify_email_snackbar),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } else {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.incorrect_login_snackbar),
                    Snackbar.LENGTH_LONG
                ).show()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                findViewById<View>(R.id.loginActivity).setRenderEffect(null)
            } else {
                dimBackground!!.visibility = View.GONE
            }
            progressBar!!.visibility = View.GONE
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
        editTextEmail = findViewById(R.id.textEmail1)
        editTextPassword = findViewById(R.id.textPassword1)
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
        }
        remember?.setOnCheckedChangeListener { buttonView, _ ->
            if (buttonView.isChecked) {
                getSharedPreferences("checkbox", MODE_PRIVATE).edit().putString("remember", "true")
                    .apply()
            } else if (!buttonView.isChecked) {
                val editor = getSharedPreferences("checkbox", MODE_PRIVATE).edit()
                editor.putString("remember", "false")
                editor.apply()
            }
        }
    }
}