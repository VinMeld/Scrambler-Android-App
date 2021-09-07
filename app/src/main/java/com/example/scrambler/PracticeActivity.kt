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
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.Executors

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
        Log.e(TAG, "enterscramble is it null: $enterScramble")
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
                                if (enterScramble!!.text.toString().lowercase().trim()
                                        .contentEquals(word.lowercase())
                                    && word != ""
                                ) {
                                    Log.e(TAG, "Correct")
                                    correctBool = true
                                }
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
            val wordNumber = randomNumber(1, 1000)
            // Instantiate the RequestQueue.
            val queue = Volley.newRequestQueue(this@PracticeActivity)
            val url = "https://most-common-words.herokuapp.com/api/search?top=" +
                    wordNumber
            Log.e(TAG, "in word call")
            val stringRequest = StringRequest(Request.Method.GET, url,
                { stringResponse ->
                    lastWord = word
                    val parts = stringResponse.split(":").toTypedArray()[1]
                    word = parts.split(",").toTypedArray()[0].replace("\"", "")
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
                },
                { volleyError ->
                    // handle error
                    Log.e(TAG, "Error in getting word $volleyError")
                }
            )
            queue.add(stringRequest)
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