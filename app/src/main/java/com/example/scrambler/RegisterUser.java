package com.example.scrambler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaCodec;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;

public class RegisterUser extends AppCompatActivity implements View.OnClickListener{
    private TextView banner, registerUser;
    private EditText editTUsername, editTextEmail, editTextPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        mAuth = FirebaseAuth.getInstance();
        banner = findViewById(R.id.textScramble);
        banner.setOnClickListener(this);

        registerUser = findViewById(R.id.buttonRegisterUser);
        registerUser.setOnClickListener(this);

        editTUsername =findViewById(R.id.textUsername);
        editTextEmail =findViewById(R.id.textEmail);
        editTextPassword =findViewById(R.id.textPassword);
        progressBar = findViewById(R.id.progressBar);
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.textScramble:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.buttonRegisterUser:
                registerUser();
                break;
        }
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String username = editTUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        if(username.isEmpty()){
            editTUsername.setError("Username required!");
            editTUsername.requestFocus();
            return;
        }
        if(email.isEmpty()){
            editTextEmail.setError("Email required!");
            editTextEmail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Please provide a valid email!");
            editTextEmail.requestFocus();
            return;
        }
        if(password.isEmpty()){
            editTextPassword.setError("Password required!");
            editTextPassword.requestFocus();
            return;
        }
        if(password.length() < 6){
            editTextPassword.setError("Min password is 6 characters!");
            editTextPassword.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            System.out.println(username + " " + email);
                            User user = new User(username, email);
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(RegisterUser.this, "User has been registered successfully!", Toast.LENGTH_LONG).show();
                                    }else{
                                        Toast.makeText(RegisterUser.this, "Failed to register! Try again!", Toast.LENGTH_LONG).show();
                                    }
                                    progressBar.setVisibility(View.GONE);
                                }
                            });

                        }else{
                            Toast.makeText(RegisterUser.this, "Failed to register! Try again!", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });

    }
}