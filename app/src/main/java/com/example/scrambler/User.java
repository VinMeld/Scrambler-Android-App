package com.example.scrambler;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    public String username, email, uuid;
    public User(){ }
    public List<Integer> scores;
    final String TAG = "MainActivity";

    public User(String username, String email){
        this.username = username;
        this.email = email;
    }

    public void setUuid(String UUID) {
        this.uuid = UUID;
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("scores", Collections.singletonList(0));

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users").document(uuid)
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
}
