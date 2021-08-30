package com.example.scrambler;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {
    String word = "";
    int correct = 0;
    int chances = 3;
     int seconds = 0;
     TimerTask task;
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_game);
            Button menu = findViewById(R.id.buttonMenu);
            TextView correctWords = findViewById(R.id.textViewCorrect);
            TextView scrambledWord = findViewById(R.id.textWord);
            EditText enterScramble = findViewById(R.id.textViewWord);
            TextView textViewChances = findViewById(R.id.textViewChances);
            Button buttonRestart = findViewById(R.id.buttonRestart);
            correctWords.setText(String.valueOf(correct));
            textViewChances.setText(String.valueOf(chances));
            startGame(scrambledWord, enterScramble, textViewChances);
            buttonRestart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chances = 3;
                    correct = 0;
                    correctWords.setText(String.valueOf(correct));
                    textViewChances.setText(String.valueOf(chances));
                    enterScramble.setVisibility(View.VISIBLE);
                    startGame(scrambledWord, enterScramble, textViewChances);
                }
            });
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(GameActivity.this, MenuActivity.class));
                }
            });
            enterScramble.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    System.out.println(enterScramble.getText());
                    System.out.println(scrambledWord.getText());
                    if (String.valueOf(enterScramble.getText()).contentEquals(word)){
                        correct++;
                        correctWords.setText(String.valueOf(correct));
                        enterScramble.setText("");
                        word = setScrambledWord(scrambledWord);
                    }
                    return false;
                }
            });

        }
    protected int randomNumber(int low, int high){
        Random r = new Random();
        return r.nextInt(high-low) + low;
    }

    protected String setScrambledWord(TextView scrambledWord){
        int wordNumber = randomNumber(1,1000);
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://most-common-words.herokuapp.com/api/search?top=" +
                wordNumber;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        String parts = response.split(":")[1];
                        word = parts.split(",")[0].replace("\"", "");

                        char[] arrayOfCharacters = word.toCharArray();
                        System.out.println(word);
                        while (word.contentEquals(String.valueOf(arrayOfCharacters))) {
                            for (int index = 0; index < arrayOfCharacters.length - 2; index++) {
                                int randomCharacter = randomNumber(0, arrayOfCharacters.length - 1);
                                int randomMover = randomNumber(0, arrayOfCharacters.length - 1);
                                char temp = arrayOfCharacters[randomCharacter];
                                arrayOfCharacters[randomCharacter] = arrayOfCharacters[randomMover];
                                arrayOfCharacters[randomMover] = temp;
                            }
                        }
                        String randomWordScrambled = String.valueOf(arrayOfCharacters);
                        // Display the first 500 characters of the response string.
                        scrambledWord.setText(randomWordScrambled);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                scrambledWord.setText("That didn't work!");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        return word;
    }
    protected void startGame(TextView scrambledWord, EditText enterScramble, TextView textViewChances){
        while (chances > 0) {
            task = new TimerTask() {
                @Override
                public void run() {
                    int MAX_SECONDS = 15;
                    if (seconds < MAX_SECONDS) {
                        seconds++;
                    } else {
                        if (chances == 1) {
                            scrambledWord.setText("You lose. Go back to menu!");
                            enterScramble.setVisibility(View.INVISIBLE);
                        }
                        word = setScrambledWord(scrambledWord);
                        chances--;
                        textViewChances.setText(String.valueOf(chances));
                        cancel();
                    }
                }
            };
        }
    }
}
