package com.example.picster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.picster.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private final String TAG = RegisterActivity.class.getName();
    EditText newUsername, newEmail, newPassword, doublecheckPassword;
    Button registerButton, alreadyHave;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        newUsername = findViewById(R.id.newUsername);
        newEmail = findViewById(R.id.newEmail);
        newPassword = findViewById(R.id.newPassword);
        doublecheckPassword = findViewById(R.id.doublecheckPassword);
        registerButton = findViewById(R.id.registerButton);
        alreadyHave = findViewById(R.id.alreadyHave);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = newEmail.getText().toString();
                String password = newPassword.getText().toString();
                String checkPassword = doublecheckPassword.getText().toString();
                String username = newUsername.getText().toString();

                if (!email.equals("") && !password.equals("")) {
                    if(password.length() >= 6) {
                        if(password.equals(checkPassword)) {
                            FirebaseFirestore database = FirebaseFirestore.getInstance();
                            database.collection("User")
                                    .whereEqualTo("username", username)
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            if (!task.getResult().isEmpty()) {
                                                Toast.makeText(RegisterActivity.this, "Already exists username", Toast.LENGTH_LONG).show();
                                            } else {
                                                saveUser(email, password, username);
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(RegisterActivity.this, "Please double-check password", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Password length must exceed 6", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Please input email and password", Toast.LENGTH_LONG).show();
                }
            }
        });

        alreadyHave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void saveUser(String email, String password, String username) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    writeNewUser(email, password, username);

                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                    Toast.makeText(RegisterActivity.this, "Success to register!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    Toast.makeText(RegisterActivity.this, "Already exists email address.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void writeNewUser(String email, String password, String username) {
        User user = new User(email, password, username);

        FirebaseFirestore database = FirebaseFirestore.getInstance();

        database.collection("User").document(email).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                });
    }

    private void checkUsername(String email, String password, String username) {

    }
}