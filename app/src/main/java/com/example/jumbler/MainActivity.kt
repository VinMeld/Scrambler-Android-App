package com.example.jumbler

import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.jumbler.utils.Jumbler
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val editTextEmail: EditText by lazy { findViewById(R.id.textEmail1) }
    private val editTextPassword: EditText by lazy { findViewById(R.id.textPassword1) }
    private val preferencesEmail: SharedPreferences by lazy {
        getSharedPreferences(
            "email",
            MODE_PRIVATE
        )
    }
    private val preferencesPassword: SharedPreferences by lazy {
        getSharedPreferences(
            "password",
            MODE_PRIVATE
        )
    }
    private val progressBar: ProgressBar by lazy { findViewById(R.id.progressBar) }
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var sqLiteDatabaseObj: SQLiteDatabase
    override fun onClick(v: View) {
        when (v.id) {
            R.id.textRegister -> startActivity(Intent(this, RegisterUser::class.java))
            R.id.buttonLogin -> userLogin()
            R.id.textForgot -> startActivity(Intent(this, ForgotPassword::class.java))
        }
    }

    private fun userLogin() {
        val email: String = editTextEmail.text.toString().trim { it <= ' ' }
        val password: String = editTextPassword.text.toString().trim { it <= ' ' }
        if (email.isEmpty()) {
            editTextEmail.error = getString(R.string.empty_email)
            editTextEmail.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.error = getString(R.string.invalid_email)
            editTextEmail.requestFocus()
            return
        }
        if (password.isEmpty()) {
            editTextPassword.error = getString(R.string.empty_password)
            editTextPassword.requestFocus()
            return
        }

        val dimBackground: LinearLayout = findViewById(R.id.dimBackground)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            findViewById<View>(R.id.loginActivity).setRenderEffect(
                RenderEffect.createBlurEffect(
                    16F,
                    16F,
                    Shader.TileMode.MIRROR
                )
            )
        } else {
            dimBackground.visibility = View.VISIBLE
        }
        progressBar.visibility = View.VISIBLE

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    (this.application as Jumbler).setCurrentUser(user.uid)
                    if (user.isEmailVerified) {
                        val editorEmail = preferencesEmail.edit()
                        editorEmail.putString("email", email)
                        editorEmail.apply()
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
                dimBackground.visibility = View.GONE
            }
            progressBar.visibility = View.GONE
        }
    }

    override fun onPause() {
        progressBar.visibility = View.GONE
        super.onPause()
    }

    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network: Network = cm.activeNetwork ?: return false
            val activeNetwork = cm.getNetworkCapabilities(network) ?: return false
            return (activeNetwork.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            ) || activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    || activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH))
        } else {
            return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
        }
    }

    private fun generateFileWords(length: Int, file: File) {
        val scopeTimer = CoroutineScope(CoroutineName("Timer"))
        scopeTimer.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            var isSame = false
            var wait = 0
            val queue1: RequestQueue = Volley.newRequestQueue(this@MainActivity)
            val url1 = "https://vinaycat.pythonanywhere.com/getcountlengthword?length=$length"
            val stringRequest1 = StringRequest(
                Request.Method.GET, url1,
                { stringResponse ->
                    Log.e("TAG", stringResponse)
                    val count: Int = stringResponse.toInt()
                    val inputAsString: String = try {
                        FileInputStream(file).bufferedReader().use { it.readText() }
                    } catch (e: FileNotFoundException) {
                        ""
                    }
                    val inputCount: Int = inputAsString.split(" ").size
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
                val queue: RequestQueue = Volley.newRequestQueue(this@MainActivity)
                val url =
                    "https://vinaycat.pythonanywhere.com/getlengthword?length=$length"
                val stringRequest = StringRequest(
                    Request.Method.GET, url,
                    { stringResponse ->
                        Log.e("TAG", stringResponse)
                        val listOfWords: List<String> = stringResponse.split(",")
                        Log.e("TAG", file.absolutePath)
                        file.parentFile.mkdirs()
                        file.createNewFile()
                        File(file.absolutePath).printWriter().use { out ->
                            listOfWords.forEach {
                                out.println("${it}\n")
                            }
                        }
                        val inputAsString: String =
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

    private fun createFilesAndGenerate() {
        Log.e("|TAG", filesDir.toString())
        val letDirectory = File(filesDir, "wordsData")
        for (i in 2..12) {
            Log.e("TAG", "words$i.txt")
            val file = File(letDirectory, "words$i.txt")
            generateFileWords(i, file)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val sharedPreferences: SharedPreferences =
            this.getSharedPreferences("displayMode", MODE_PRIVATE)
        when {
            sharedPreferences.getString("displayMode", null) == "light" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            sharedPreferences.getString("displayMode", null) == "dark" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        super.onCreate(savedInstanceState)
        if (isNetworkConnected()) {
            createFilesAndGenerate()
            createDictionaryDatabase()
        } else {
            (this.application as Jumbler).setIsOffline(true)
            startActivity(Intent(this@MainActivity, MenuActivity::class.java))
        }
        setContentView(R.layout.activity_main)
        val loginView: RelativeLayout = findViewById(R.id.loginActivity)
        val appLaunchProgressView: RelativeLayout = findViewById(R.id.appLaunchProgress)
        val remember: CheckBox = findViewById(R.id.checkBox)
        val forgotPassword: TextView = findViewById(R.id.textForgot)
        forgotPassword.setOnClickListener(this)
        val register: TextView = findViewById(R.id.textRegister)
        register.setOnClickListener(this)
        val signIn: Button = findViewById(R.id.buttonLogin)
        signIn.setOnClickListener(this)

        val preferences: SharedPreferences = getSharedPreferences("checkbox", MODE_PRIVATE)
        val checkbox: String = preferences.getString("remember", "").toString()
        val email: String = preferencesEmail.getString("email", "").toString()
        val password: String = preferencesPassword.getString("password", "").toString()

        if (checkbox == "true" && email != "" && password != "") {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        (this.application as Jumbler).setCurrentUser(user.uid)
                        startActivity(Intent(this@MainActivity, MenuActivity::class.java))
                    }
                }
            }
        } else {
            appLaunchProgressView.visibility = View.GONE
            loginView.visibility = View.VISIBLE
        }

        remember.setOnCheckedChangeListener { buttonView, _ ->
            if (buttonView.isChecked) {
                getSharedPreferences("checkbox", MODE_PRIVATE).edit().putString("remember", "true")
                    .apply()
            } else if (!buttonView.isChecked) {
                val editor: SharedPreferences.Editor =
                    getSharedPreferences("checkbox", MODE_PRIVATE).edit()
                editor.putString("remember", "false")
                editor.apply()
            }
        }
    }

    private fun createDictionaryDatabase() {
        val letDirectory = File(filesDir, "dictData")
        val file = File(letDirectory, "dictionary.txt")
        file.parentFile.mkdirs()
        file.createNewFile()
        Log.e("TAG", "Creating database");
        val inputAsString: String = try {
            FileInputStream(file).bufferedReader().use { it.readText() }
        } catch (e: FileNotFoundException) {
            ""
        }
        if (inputAsString == "") {
            Log.e("TAG", "Creating database, string empty");
            val scopeAddWords = CoroutineScope(CoroutineName("Timer"))
            scopeAddWords.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
                val queue1: RequestQueue = Volley.newRequestQueue(this@MainActivity)
                val url1 = "https://vinaycat1.pythonanywhere.com/return"
                val stringRequest1 = StringRequest(
                    Request.Method.GET, url1,
                    { stringResponse ->
                        Log.e("TAG", "Creating database, request was successful");
                        Log.e("TAG", stringResponse);
                        File(file.absolutePath).printWriter().use { out ->
                            out.println(stringResponse)
                        }
                        val inputAsString: String =
                            FileInputStream(file).bufferedReader().use { it.readText() }
                        Log.e("TAG", "Outputing file");
                        Log.e("TAG", inputAsString);
                    },
                    { volleyError ->
                        // handle error
                        Log.e("TAG", "Error in getting word $volleyError")
                    }
                )
                queue1.add(stringRequest1)
            }
        }
    }


    private fun readDataFromDatabase(word: String): Boolean {
        // val cursorCourses: Cursor = sqLiteDatabaseObj.rawQuery("SELECT $COL_NAME FROM $TABLENAME WHERE $COL_NAME = '$word'", null);
//        while (cursorCourses.moveToNext()) {
//            if(cursorCourses.getString(0) == word){
//                return true;
//            }
//            return false;
//        }
//        return false;
        val letDirectory = File(filesDir, "dictData")
        val file = File(letDirectory, "dictionary.txt")
        val inputAsString: String = try {
            FileInputStream(file).bufferedReader().use { it.readText() }
        } catch (e: FileNotFoundException) {
            ""
        }
        Log.e("TAG", inputAsString)
        return inputAsString.contains(" $word ")
    }
}