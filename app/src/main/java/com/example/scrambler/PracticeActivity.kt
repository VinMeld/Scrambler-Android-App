package com.example.scrambler

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.FirebaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.Executors
import androidx.annotation.NonNull







open class PracticeActivity : AppCompatActivity(), View.OnClickListener {
    private var word = ""
    private var randomWordScrambled = ""
    private var correct = 0
    private var menu: Button? = null
    private var correctWords: TextView? = null
    private var scrambledWord: TextView? = null
    private var enterScramble: EditText? = null
    private var buttonRestart: Button? = null
    private var lastWord = ""
    private val scopeTimer = CoroutineScope(CoroutineName("Timer"))
    private var apiKey: String? = null
    private var wordsArray = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practice)
        menu = findViewById(R.id.buttonPracticeMenu)
        menu?.setOnClickListener(this)
        correctWords = findViewById(R.id.textViewPracticeCorrect)
        scrambledWord = findViewById(R.id.practiceTextWord)
        enterScramble = findViewById(R.id.editTextWord)
        buttonRestart = findViewById(R.id.buttonPracticeRestart)
        apiKey = getString(R.string.parse_application_id)
        val wordsRef = FirebaseDatabase.getInstance().getReference("WordList").child("listOfWords")
        wordsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (ds in dataSnapshot.children) {
                        wordsArray.add(ds.value as String)
                        Log.d("TAG", ds.value as String)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.toString())
            }
        })


        runOnUiThread {
            correctWords?.text = correct.toString()
        }
        startGame()
        buttonRestart!!.setOnClickListener {
            Log.e(TAG, "skip")
            startGame()
        }
        enterScramble!!.setOnKeyListener { _, _, _ ->
            checkIfCorrect()
            false
        }
    }

    private fun checkIfCorrect() {
        var correctBool = false
        if (word != "" || lastWord != word) {
            if (enterScramble?.text.toString().trim().length == word.length) {
                if (sameChars(
                        enterScramble!!.text.toString().trim().lowercase(),
                        word.lowercase()
                    )

                ) {
                    if (enterScramble!!.text.toString().lowercase().trim()
                            .contentEquals(word.lowercase())
                        && word != ""
                    ) {
                        Log.e(TAG, "Correct")
                        correctBool = true
                    }
                    if(!correctBool){
                        val queue = Volley.newRequestQueue(this@PracticeActivity)
                        val enteredWord = enterScramble?.text.toString().trim()
                        val url =
                            "https://www.dictionaryapi.com/api/v3/references/sd2/json/$enteredWord?key=$apiKey"
                        val stringRequest = StringRequest(
                            Request.Method.GET, url,
                            { stringResponse ->
                                Log.e(TAG, stringResponse)
                                if (stringResponse.contains("meta")) {
                                    Log.e(TAG, "Guessed a correct word from webster")
                                    correctBool = true

                                }
                                if (correctBool) {
                                    // If they got it right then cancel guess and restart
                                    correct++
                                    runOnUiThread {
                                        enterScramble!!.text.clear()
                                        correctWords!!.text = getString(R.string.score, correct)
                                    }
                                    startGame()
                                }
                            },
                            { volleyError ->
                                // handle error
                                Log.e(TAG, "Error in getting word $volleyError")
                            }
                        )
                        queue.add(stringRequest)

                    } else {
                        // If they got it right then cancel guess and restart
                        correct++
                        runOnUiThread {
                            enterScramble!!.text.clear()
                            correctWords!!.text = getString(R.string.score, correct)
                        }
                        startGame()
                        }
                    }
                }
            }
        }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonPracticeMenu -> startActivity(Intent(this, MenuActivity::class.java))
        }
    }

    private fun randomNumber(low: Int, high: Int): Int {
        val r = Random()
        return r.nextInt(high - low) + low
    }

    @SuppressLint("SetTextI18n")
    protected fun startGame() {
        Log.e(TAG, "StartGame")
        scopeTimer.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            while(wordsArray.size < 1){
                delay(100L)
            }
           val wordNumber = randomNumber(1, wordsArray.size)

            lastWord = word
            word = wordsArray[wordNumber]
            val random = Random()
            Log.e(TAG, "Resetting randomwordscramble")
            val a: CharArray = word.toCharArray()
            for (i in a.indices) {
                val j: Int = random.nextInt(a.size)
                // Swap letters
                val temp = a[i]
                a[i] = a[j]
                a[j] = temp
            }
            randomWordScrambled = String(a)
            if (word.length == 1) {
                randomWordScrambled = word
            }
            // Display timer and set random word

            if (scrambledWord != null) {
                Log.e(TAG, "Resetting randomwordscramble2")
                runOnUiThread {
                    scrambledWord!!.text = randomWordScrambled
                }
            }
        }
    }

    companion object {
        private const val TAG = "PracticeActivity"
    }

    override fun onPause() {
        Log.e(TAG, "in pause()")
        runBlocking {
            scopeTimer.cancel()
        }
        super.onPause()
    }

    private fun sameChars(firstStr: String, secondStr: String): Boolean {
        val first = firstStr.toCharArray()
        val second = secondStr.toCharArray()
        Arrays.sort(first)
        Arrays.sort(second)
        return first.contentEquals(second)
    }
}