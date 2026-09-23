package com.example.smartduetadmissionsystem;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class UniversityActivity extends AppCompatActivity {

    private TextView tvBack;

    private MaterialCardView cardAbout;
    private MaterialCardView cardDepartments;
    private MaterialCardView cardSeats;
    private MaterialCardView cardRequirements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_university);


        tvBack = findViewById(R.id.tvBackUniversity);

        cardAbout = findViewById(R.id.cardAboutDuet);
        cardDepartments = findViewById(R.id.cardDepartmentList);
        cardSeats = findViewById(R.id.cardSeats);
        cardRequirements = findViewById(R.id.cardRequirements);


        // Back

        tvBack.setOnClickListener(v -> finish());


        // About DUET

        cardAbout.setOnClickListener(v -> {
            // Details can be added later
        });


        // Departments

        cardDepartments.setOnClickListener(v -> {
            // Department details can be added later
        });


        // Available Seats

        cardSeats.setOnClickListener(v -> {
            // Seat information can be added later
        });


        // Admission Requirements

        cardRequirements.setOnClickListener(v -> {

            startActivity(new android.content.Intent(
                    UniversityActivity.this,
                    EligibilityActivity.class
            ));
        });
    }
}