package com.example.scrambler;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class GameActivity extends AppCompatActivity {
    private static final String TAG = "GameActivity";
    String word = "";
    int correct = 0;
    int chances = 3;
    int seconds = 0;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    String userID = firebaseUser.getUid();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference user = db.collection("Users").document(userID);
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
        TextView timerText = findViewById(R.id.textViewTimer);
        word = setScrambledWord(scrambledWord);
        startGame(scrambledWord, enterScramble, textViewChances, timerText);
        // RESTART BUTTON
        buttonRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(chances > 0){
                    Toast toast = Toast.makeText(getApplicationContext(), "Keep trying! You're not done yet", Toast. LENGTH_SHORT);
                    toast.show();
                } else {
                    chances = 3;
                    correct = 0;
                    correctWords.setText(String.valueOf(correct));
                    textViewChances.setText(String.valueOf(chances));
                    enterScramble.setVisibility(View.VISIBLE);
                    scrambledWord.setVisibility(View.VISIBLE);
                    word = setScrambledWord(scrambledWord);
                    startGame(scrambledWord, enterScramble, textViewChances, timerText);
                }
            }
        });
        // MENU
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GameActivity.this, MenuActivity.class));
            }
        });
        // CHECK IF TYPED CORRECTLY
        enterScramble.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                // IF ANSWER IS CORRECT
                if (String.valueOf(enterScramble.getText()).contentEquals(word)) {
                    seconds = 1;
                    correct++;
                    correctWords.setText(String.valueOf(correct));
                    enterScramble.setText("");
                    word = setScrambledWord(scrambledWord);

                }
                return false;
            }
        });

    }

    protected int randomNumber(int low, int high) {
        Random r = new Random();
        return r.nextInt(high - low) + low;
    }

    protected String setScrambledWord(TextView scrambledWord) {
        int wordNumber = randomNumber(1, 1000);
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://most-common-words.herokuapp.com/api/search?top=" +
                wordNumber;
//        if(word.isEmpty()){
//            word = "a";
//        }
        // while (word.length() < 3) {
            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            String parts = response.split(":")[1];
                            word = parts.split(",")[0].replace("\"", "");
                            Log.e(TAG, word);
                            if(word.length() < 3){
                                char[] arrayOfCharacters = word.toCharArray();
                                String randomWordScrambled = new String(new char[]{arrayOfCharacters[1],arrayOfCharacters[0]});
                                scrambledWord.setText(randomWordScrambled);
                            } else {
                                char[] arrayOfCharacters = word.toCharArray();
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

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    scrambledWord.setText("That didn't work!");
                }
            });
            // Add the request to the RequestQueue.
            queue.add(stringRequest);
       // }
        return word;
    }

    protected void startGame(TextView scrambledWord, EditText enterScramble, TextView textViewChances, TextView timerText) {
        Log.e(TAG, "StartGame");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (seconds < 10) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timerText.setText(String.valueOf(seconds));
                        }
                    });
                    Log.e(TAG, "Seconds = " + seconds);
                    seconds++;
                } else {
                    if (chances == 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                enterScramble.setVisibility(View.INVISIBLE);
                                scrambledWord.setVisibility(View.INVISIBLE);
                            }
                        });
                        timer.cancel();
                        // ADDING SCORE TO DATABASE
                        user.update("scores", FieldValue.arrayUnion(correct));
                    }
                    word = setScrambledWord(scrambledWord);
                    chances--;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textViewChances.setText(String.valueOf(chances));
                        }
                    });
                    seconds = 1;
                }
            }
        }, 0, 1000);
    }
}
