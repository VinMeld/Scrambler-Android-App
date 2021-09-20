package com.example.jumbler

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
import java.io.File
import java.io.FileInputStream
import java.lang.Thread.sleep
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
    private var buttonHint: Button? = null
    private var lastWord = ""
    private val scopeTimer = CoroutineScope(CoroutineName("Timer"))
    private var apiKey: String? = null
    private var wordListLength = arrayOf<Array<String>>()
    private var hintNumber = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practice)
        buttonHint = findViewById(R.id.buttonHint)
        menu = findViewById(R.id.buttonPracticeMenu)
        menu?.setOnClickListener(this)
        correctWords = findViewById(R.id.textViewPracticeCorrect)
        scrambledWord = findViewById(R.id.practiceTextWord)
        enterScramble = findViewById(R.id.editTextWord)
        buttonRestart = findViewById(R.id.buttonPracticeRestart)
        apiKey = getString(R.string.parse_application_id)
        val path = filesDir
        val letDirectory = File(path, "wordsData")

        for(i in 2..12){
            val file = File(letDirectory, "words$i.txt")
            val inputAsString = FileInputStream(file).bufferedReader().use { it.readText() }
            wordListLength += (inputAsString.split(" ") as MutableList<String>).toTypedArray()
        }
        runOnUiThread {
            correctWords?.text = correct.toString()
        }
        startGame()
        checkScrambler()
        buttonRestart!!.setOnClickListener {
            Log.e(TAG, "skip")
            hintNumber = 0
            runOnUiThread {
                enterScramble!!.text.clear()
                correctWords!!.text = getString(R.string.score, correct)
            }
            startGame()
        }
        buttonHint!!.setOnClickListener {
                val scopeTimer = CoroutineScope(CoroutineName("Timer"))
                scopeTimer.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
                    if (word.length == hintNumber + 1) {
                        hintNumber++
                        val hintWord = word.substring(0, hintNumber)
                        runOnUiThread {
                            Log.e(TAG, "setting hint full")
                            correctWords?.text = hintWord
                        }
                        hintNumber = 0
                        delay(1000L)
                        runOnUiThread {
                            enterScramble!!.text.clear()
                            correctWords!!.text = getString(R.string.score, correct)
                        }
                        startGame()
                    } else {
                        hintNumber++
                        val hintWord = word.substring(0, hintNumber)
                        runOnUiThread {
                            correctWords?.text = hintWord
                        }
                    }
            }
        }
    }

    private fun checkScrambler() {
        val scopeTimer = CoroutineScope(CoroutineName("Timer"))
        scopeTimer.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            var lastGuess = ""
            while (true) {
                if (enterScramble!!.text.length == word.length && lastGuess != enterScramble!!.text.toString()) {
                    checkIfCorrect()
                }
                lastGuess = enterScramble!!.text.toString()
                delay(100L)
            }
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
                    if (!correctBool) {
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
                                    if (hintNumber == 0) {
                                        correct++
                                    }
                                    hintNumber = 0
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
                        if (hintNumber == 0) {
                            correct++
                        }
                        hintNumber = 0
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
    private fun generateWord(first :Int, second : Int, third : Int){
        val randomNum = randomNumber(1, 3)
        if(randomNum == 1) {
            val randomNum1 = randomNumber(0, wordListLength[first].size)
            word = wordListLength[first][randomNum1]
        } else if (randomNum == 2){
            val randomNum1 = randomNumber(0, wordListLength[second].size)
            word = wordListLength[second][randomNum1]
        } else if (randomNum == 3){
            val randomNum1 = randomNumber(0, wordListLength[third].size)
            word = wordListLength[third][randomNum1]
        }
    }
    @SuppressLint("SetTextI18n")
    protected fun startGame() {
        Log.e(TAG, "StartGame")
        scopeTimer.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            lastWord = word
            if(correct in 0..5){
                generateWord(1,1,2)
            }
            if(correct in 6..10){
                val newRandomNum = randomNumber(1, 10)
                if(newRandomNum in 1..3) {
                    generateWord(2,2,3)
                } else {
                    generateWord(3,4,5)
                }
            }
            if(correct in 11..15){
                val newRandomNum = randomNumber(1, 10)
                if(newRandomNum in 1..2) {
                    generateWord(2,3,3)
                } else if(newRandomNum in 3..4) {
                    generateWord(4,4,5)
                } else{
                    generateWord(6,8,8)
                }
            }
            if(correct in 16..20) {
                val newRandomNum = randomNumber(1, 20)
                if (newRandomNum in 1..2) {
                    generateWord(3,3,4)
                } else if (newRandomNum in 3..4) {
                    generateWord(3, 4, 5)
                } else if (newRandomNum in 5..10) {
                    generateWord(6, 7, 8)
                } else {
                    generateWord(9, 10, 11)
                }
            }
            if(correct > 20){
                val newRandomNum = randomNumber(1, 30)
                if (newRandomNum in 1..2) {
                    generateWord(5,5,5)
                } else if (newRandomNum in 3..10) {
                    generateWord(6, 6, 7)
                } else if (newRandomNum in 11..20) {
                    generateWord(7, 7, 8)
                } else {
                    generateWord(9, 10, 11)
                }
            }
            randomWordScrambled = word
            while(randomWordScrambled == word) {
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
            }

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