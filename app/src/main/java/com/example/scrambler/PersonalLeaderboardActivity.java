package com.example.scrambler;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class PersonalLeaderboardActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_leaderboard);
        Button menu = findViewById(R.id.buttonMenuLeaderboard);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PersonalLeaderboardActivity.this, MenuActivity.class));
            }
        });
    }
}
