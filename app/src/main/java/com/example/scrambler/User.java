package com.example.scrambler;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class User {
    public String username, email, UUID;
    public User(){ }

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

        db.collection("cities").document(UUID)
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
