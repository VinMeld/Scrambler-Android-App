package com.example.scrambler;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class User {
    public String username, email, UUID;
    public User(){ }
    public int[] scores;

    public User(String username, String email){
        this.username = username;
        this.email = email;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        final String TAG = "MainActivity";
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users").document(UUID)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {

                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }
    public void addScore(int score){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference user = db.collection("Users").document(UUID);
        user.update("scores", FieldValue.arrayUnion(score));
    }
    public int[] readScores() {
        final String TAG = "MainActivity";
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference user = db.collection("Users").document(UUID);
        user.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot userSnapShot = task.getResult();
                    if (userSnapShot != null) {
                        String test = userSnapShot.getString("scores");
                        Log.e(TAG, test);
                    } else {
                        Log.d("LOGGER", "No such document");
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });
        return scores;
    }
}
