package com.example.smartduetadmissionsystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private TextInputEditText etRegisterEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etRegisterPassword;
    private TextInputEditText etConfirmPassword;

    private CheckBox cbTerms;

    private MaterialButton btnRegister;

    private TextView tvBack;
    private TextView tvBackToLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        // =============================================
        // FIREBASE
        // =============================================

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // =============================================
        // CONNECT XML
        // =============================================

        etName = findViewById(R.id.etName);
        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        etPhone = findViewById(R.id.etPhone);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        cbTerms = findViewById(R.id.cbTerms);

        btnRegister = findViewById(R.id.btnRegister);

        tvBack = findViewById(R.id.tvBack);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // =============================================
        // REGISTER
        // =============================================

        btnRegister.setOnClickListener(v -> registerUser());

        // =============================================
        // BACK TO LOGIN
        // =============================================

        tvBack.setOnClickListener(v -> goToLogin());

        tvBackToLogin.setOnClickListener(v -> goToLogin());
    }

    // =================================================
    // REGISTER USER
    // =================================================

    private void registerUser() {

        String name = etName.getText()
                .toString()
                .trim();

        String email = etRegisterEmail.getText()
                .toString()
                .trim()
                .toLowerCase();

        String phone = etPhone.getText()
                .toString()
                .trim();

        String password = etRegisterPassword.getText()
                .toString();

        String confirmPassword = etConfirmPassword.getText()
                .toString();

        // =============================================
        // NAME
        // =============================================

        if (TextUtils.isEmpty(name)) {

            etName.setError("Enter your full name");
            etName.requestFocus();
            return;
        }

        // =============================================
        // EMAIL
        // =============================================

        if (TextUtils.isEmpty(email)) {

            etRegisterEmail.setError(
                    "Enter your email address"
            );

            etRegisterEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            etRegisterEmail.setError(
                    "Enter a valid email address"
            );

            etRegisterEmail.requestFocus();
            return;
        }

        // =============================================
        // PHONE
        // =============================================

        if (TextUtils.isEmpty(phone)) {

            etPhone.setError(
                    "Enter your mobile number"
            );

            etPhone.requestFocus();
            return;
        }

        if (!phone.matches("\\d{11}")) {

            etPhone.setError(
                    "Enter a valid 11-digit number"
            );

            etPhone.requestFocus();
            return;
        }

        // =============================================
        // PASSWORD
        // =============================================

        if (TextUtils.isEmpty(password)) {

            etRegisterPassword.setError(
                    "Create a password"
            );

            etRegisterPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {

            etRegisterPassword.setError(
                    "Password must contain at least 6 characters"
            );

            etRegisterPassword.requestFocus();
            return;
        }

        // =============================================
        // CONFIRM PASSWORD
        // =============================================

        if (TextUtils.isEmpty(confirmPassword)) {

            etConfirmPassword.setError(
                    "Confirm your password"
            );

            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {

            etConfirmPassword.setError(
                    "Passwords do not match"
            );

            etConfirmPassword.requestFocus();
            return;
        }

        // =============================================
        // TERMS
        // =============================================

        if (!cbTerms.isChecked()) {

            Toast.makeText(
                    RegisterActivity.this,
                    "Please accept Terms & Conditions",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // =============================================
        // LOADING
        // =============================================

        btnRegister.setEnabled(false);

        btnRegister.setText(
                "CREATING ACCOUNT..."
        );

        // =============================================
        // CREATE FIREBASE ACCOUNT FIRST
        // =============================================

        createFirebaseAccount(
                name,
                email,
                phone,
                password
        );
    }

    // =================================================
    // CREATE FIREBASE AUTH ACCOUNT
    // =================================================

    private void createFirebaseAccount(
            String name,
            String email,
            String phone,
            String password
    ) {

        mAuth.createUserWithEmailAndPassword(
                email,
                password
        ).addOnCompleteListener(task -> {

            // =============================================
            // AUTH CREATION FAILED
            // =============================================

            if (!task.isSuccessful()) {

                String errorMessage =
                        "Registration failed. Please try again.";

                if (task.getException() != null &&
                        task.getException().getMessage() != null) {

                    String firebaseError =
                            task.getException().getMessage();

                    String lowerError =
                            firebaseError.toLowerCase();

                    if (lowerError.contains("already in use") ||
                            lowerError.contains("already exists") ||
                            lowerError.contains("email address is already")) {

                        errorMessage =
                                "This email address is already registered.";

                    } else {

                        errorMessage =
                                firebaseError;
                    }
                }

                showRegistrationError(errorMessage);
                return;
            }

            FirebaseUser user =
                    mAuth.getCurrentUser();

            if (user == null) {

                showRegistrationError(
                        "Unable to create account."
                );

                return;
            }

            // =============================================
            // UPDATE DISPLAY NAME
            // =============================================

            UserProfileChangeRequest profileUpdates =
                    new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(profileTask -> {

                        /*
                         * Continue even if profile update fails.
                         */

                        checkPhoneAvailability(
                                user,
                                name,
                                email,
                                phone
                        );
                    });
        });
    }

    // =================================================
    // CHECK PHONE AFTER AUTHENTICATION
    // =================================================

    private void checkPhoneAvailability(
            FirebaseUser user,
            String name,
            String email,
            String phone
    ) {

        btnRegister.setText(
                "CHECKING MOBILE NUMBER..."
        );

        db.collection("phoneIndex")
                .document(phone)
                .get()
                .addOnSuccessListener(phoneSnapshot -> {

                    // =============================================
                    // PHONE ALREADY REGISTERED
                    // =============================================

                    if (phoneSnapshot.exists()) {

                        deleteNewAuthAccount(
                                user,
                                "This mobile number is already registered."
                        );

                        return;
                    }

                    // =============================================
                    // PHONE IS AVAILABLE
                    // =============================================

                    btnRegister.setText(
                            "SAVING ACCOUNT..."
                    );

                    saveUserData(
                            user,
                            name,
                            email,
                            phone
                    );
                })
                .addOnFailureListener(e -> {

                    deleteNewAuthAccount(
                            user,
                            "Unable to check mobile number. Please try again."
                    );
                });
    }

    // =================================================
    // SAVE USER DATA + RESERVE PHONE
    // =================================================

    private void saveUserData(
            FirebaseUser user,
            String name,
            String email,
            String phone
    ) {

        String uid = user.getUid();

        // =============================================
        // REFERENCES
        // =============================================

        DocumentReference phoneReference =
                db.collection("phoneIndex")
                        .document(phone);

        DocumentReference userReference =
                db.collection("users")
                        .document(uid);

        // =============================================
        // USER DATA
        // =============================================

        Map<String, Object> userData =
                new HashMap<>();

        userData.put("name", name);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("uid", uid);

        // =============================================
        // PHONE INDEX DATA
        // =============================================

        Map<String, Object> phoneData =
                new HashMap<>();

        phoneData.put("uid", uid);
        phoneData.put("phone", phone);
        phoneData.put("email", email);

        // =============================================
        // FIRESTORE TRANSACTION
        // =============================================

        db.runTransaction(transaction -> {

            // ---------------------------------------------
            // CHECK PHONE ONE MORE TIME
            // ---------------------------------------------

            DocumentSnapshot phoneSnapshot =
                    transaction.get(phoneReference);

            if (phoneSnapshot.exists()) {

                throw new IllegalStateException(
                        "PHONE_ALREADY_REGISTERED"
                );
            }

            // ---------------------------------------------
            // RESERVE PHONE
            // ---------------------------------------------

            transaction.set(
                    phoneReference,
                    phoneData
            );

            // ---------------------------------------------
            // SAVE USER PROFILE
            // ---------------------------------------------

            transaction.set(
                    userReference,
                    userData,
                    SetOptions.merge()
            );

            return null;

        }).addOnSuccessListener(unused -> {

            // =============================================
            // SUCCESS
            // =============================================

            Toast.makeText(
                    RegisterActivity.this,
                    "Account created successfully!",
                    Toast.LENGTH_SHORT
            ).show();

            Intent intent =
                    new Intent(
                            RegisterActivity.this,
                            MainActivity.class
                    );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);

            finish();

        }).addOnFailureListener(e -> {

            if ("PHONE_ALREADY_REGISTERED"
                    .equals(e.getMessage())) {

                deleteNewAuthAccount(
                        user,
                        "This mobile number is already registered."
                );

                return;
            }

            deleteNewAuthAccount(
                    user,
                    "Could not save account information. Please try again."
            );
        });
    }

    // =================================================
    // DELETE NEW AUTH ACCOUNT
    // =================================================

    private void deleteNewAuthAccount(
            FirebaseUser user,
            String message
    ) {

        user.delete()
                .addOnCompleteListener(deleteTask -> {

                    mAuth.signOut();

                    showRegistrationError(
                            message
                    );

                    etPhone.requestFocus();
                });
    }

    // =================================================
    // SHOW ERROR
    // =================================================

    private void showRegistrationError(
            String message
    ) {

        btnRegister.setEnabled(true);

        btnRegister.setText(
                "CREATE ACCOUNT"
        );

        Toast.makeText(
                RegisterActivity.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    // =================================================
    // GO TO LOGIN
    // =================================================

    private void goToLogin() {

        Intent intent =
                new Intent(
                        RegisterActivity.this,
                        LoginActivity.class
                );

        startActivity(intent);

        finish();
    }
}