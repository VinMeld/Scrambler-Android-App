package com.example.jumbler

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.jumbler.utils.Jumbler
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var dimBackground: LinearLayout? = null
    private var loginView: RelativeLayout? = null
    private var appLaunchProgressView: RelativeLayout? = null
    private var forgotPassword: TextView? = null
    private var editTextEmail: EditText? = null
    private var editTextPassword: EditText? = null
    private var signIn: Button? = null
    private var mAuth: FirebaseAuth? = null
    private var progressBar: ProgressBar? = null
    private var remember: CheckBox? = null
    private var register: TextView? = null
    private var file: File? = null
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
                    (this.application as Jumbler).setCurrentUser(user.uid)
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

    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }
    private fun generateFileWords(length: Int, file: File) {
        val scopeTimer = CoroutineScope(CoroutineName("Timer"))
        scopeTimer.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            var isSame = false
            var wait = 0
            val queue1 = Volley.newRequestQueue(this@MainActivity)
            val url1 = "https://vinaycat.pythonanywhere.com/getcountlengthword?length=$length"
            val stringRequest1 = StringRequest(
                Request.Method.GET, url1,
                { stringResponse ->
                    Log.e("TAG", stringResponse)
                    val count = stringResponse.toInt()
                    var inputAsString = ""
                    inputAsString = try {
                        FileInputStream(file).bufferedReader().use { it.readText() }
                    } catch (e: FileNotFoundException){
                        ""
                    }
                    val inputCount = inputAsString.split(" ").size
                    if (count == inputCount) {
                        isSame = true
                        Log.e("TAG", "true")
                    }
                    wait = 1
                },
                { volleyError ->
                    // handle error
                    Log.e("TAG", "Error in getting word $volleyError")
                }
            )
            queue1.add(stringRequest1)

            while (wait == 0) {
                delay(10L)
            }
            if (!isSame) {
                Log.e("TAG", "deleting isSame")
                file.delete()
                val queue = Volley.newRequestQueue(this@MainActivity)
                val url =
                    "https://vinaycat.pythonanywhere.com/getlengthword?length=$length"
                val stringRequest = StringRequest(
                    Request.Method.GET, url,
                    { stringResponse ->
                        Log.e("TAG", stringResponse)
                        val listOfWords = stringResponse.split(",")
                        for (word in listOfWords) {
                            file.appendText(word + "\n")
                        }
                        val inputAsString =
                            FileInputStream(file).bufferedReader().use { it.readText() }
                        Log.e("TAG", inputAsString)
                    },
                    { volleyError ->
                        // handle error
                        Log.e("TAG", "Error in getting word $volleyError")
                    }
                )
                queue.add(stringRequest)
            }
        }
    }
    private fun createFilesAndGenerate(){
        val path = filesDir
        Log.e("|TAG", path.toString())
        val letDirectory = File(path, "wordsData")
        for (i in 2..12){
            val file = File(letDirectory, "words$i.txt")
            generateFileWords(i, file)
        }
    }
    private fun updateAllWords() {
        val scopeTimer = CoroutineScope(CoroutineName("Timer"))
        scopeTimer.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            var isSame = false
            var wait = 0
            val queue1 = Volley.newRequestQueue(this@MainActivity)
            val url1 =
                "https://vinaycat.pythonanywhere.com/count"
            val stringRequest1 = StringRequest(
                Request.Method.GET, url1,
                { stringResponse ->
                    Log.e("TAG", stringResponse)
                    val count = stringResponse.toInt()
                    var inputAsString = ""
                    inputAsString = try {
                        FileInputStream(file).bufferedReader().use { it.readText() }
                    } catch (e: FileNotFoundException){
                        ""
                    }
                    val inputCount = inputAsString.split(" ").size
                    Log.e("TAG", inputAsString.toString())
                    if (count == inputCount) {
                        isSame = true
                        Log.e("TAG", "true")
                    }
                    wait = 1
                },
                { volleyError ->
                    // handle error
                    Log.e("TAG", "Error in getting word $volleyError")
                }
            )
            queue1.add(stringRequest1)

            while (wait == 0) {
                delay(10L)
            }
            Log.e("TAG", wait.toString())
            Log.e("TAG", isSame.toString())
            if (!isSame) {
                Log.e("TAG", "deleting isSame")
                file?.delete()
                val queue = Volley.newRequestQueue(this@MainActivity)
                val url =
                    "https://vinaycat.pythonanywhere.com/all"
                val stringRequest = StringRequest(
                    Request.Method.GET, url,
                    { stringResponse ->
                        Log.e("TAG", stringResponse)
                        val listOfWords = stringResponse.split(",")
                        for (word in listOfWords) {
                            file?.appendText(word + "\n")
                        }
                        val inputAsString =
                            FileInputStream(file).bufferedReader().use { it.readText() }
                        Log.e("TAG", inputAsString)
                    },
                    { volleyError ->
                        // handle error
                        Log.e("TAG", "Error in getting word $volleyError")
                    }
                )
                queue.add(stringRequest)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val path = filesDir
        Log.e("|TAG", path.toString())
        val letDirectory = File(path, "wordsData")
        letDirectory.mkdirs()
        file = File(letDirectory, "words.txt")
        if (isNetworkConnected()) {
            createFilesAndGenerate()
            updateAllWords()
        } else {
            (this.application as Jumbler).setIsOffline(true)
            startActivity(Intent(this@MainActivity, MenuActivity::class.java))
        }
        setContentView(R.layout.activity_main)
        loginView = findViewById(R.id.loginActivity)
        appLaunchProgressView = findViewById(R.id.appLaunchProgress)
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
                        (this.application as Jumbler).setCurrentUser(user.uid)
                        startActivity(Intent(this@MainActivity, MenuActivity::class.java))
                    }
                }
            }
        } else {
            appLaunchProgressView!!.visibility = View.GONE;
            loginView!!.visibility = View.VISIBLE;
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