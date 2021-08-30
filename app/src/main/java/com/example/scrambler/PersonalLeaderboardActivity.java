package com.example.scrambler;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.core.OrderBy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PersonalLeaderboardActivity extends AppCompatActivity {
    private static final String TAG = "PersonalLeaderboard";
    List<Integer> scores;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_leaderboard);
        Button menu = findViewById(R.id.buttonMenuLeaderboard);
        TextView leaderboard = findViewById(R.id.textViewLeaderboard);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PersonalLeaderboardActivity.this, MenuActivity.class));
            }
        });
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        String userID = firebaseUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference user = db.collection("Users").document(userID);
        user.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot userSnapShot = task.getResult();
                    if (userSnapShot != null) {
                        // Log.e(TAG, (String) userSnapShot.get("scores"));
                        scores = (List<Integer>) userSnapShot.get("scores");
                        if(scores != null){
                            Collections.sort(scores, Collections.reverseOrder());
                            StringBuilder leaderboardText = new StringBuilder();
                            for (int i = 0; i < scores.size(); i++) {
                                leaderboardText.append(i+1).append(". ").append(scores.get(i)).append("\n");
                            }
                            leaderboard.setText(leaderboardText);
                        } else{
                            leaderboard.setText("You have not gotten any correct!");
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
}
