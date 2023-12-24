package com.example.picster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class UsernameActivity extends AppCompatActivity {
    EditText newUsernameGoogle;
    Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);

        newUsernameGoogle = findViewById(R.id.newUsernameGoogle);
        startButton = findViewById(R.id.startButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = newUsernameGoogle.getText().toString().trim();
                if (username.isEmpty()) {
                    Toast.makeText(UsernameActivity.this, "Please input new username", Toast.LENGTH_SHORT).show();
                } else {
                    CollectionReference userRef = FirebaseFirestore.getInstance().collection("User");
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                    String email = currentUser.getEmail();

                    userRef.whereEqualTo("username", username)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        QuerySnapshot querySnapshot = task.getResult();
                                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                            Toast.makeText(UsernameActivity.this, "Already exists username", Toast.LENGTH_SHORT).show();
                                        } else {
                                            userRef.whereEqualTo("email", email)
                                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                QuerySnapshot querySnapshot = task.getResult();
                                                                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                                                    DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                                                                    String documentId = documentSnapshot.getId();

                                                                    userRef.document(documentId).update("username", username)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    Toast.makeText(UsernameActivity.this, "Log in Success!", Toast.LENGTH_SHORT).show();
                                                                                    Intent intent = new Intent(UsernameActivity.this, DashboardActivity.class);
                                                                                    startActivity(intent);
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Toast.makeText(UsernameActivity.this, "Failed to update username;" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        }
                                                    });
                                        }
                                    } else {
                                        Toast.makeText(UsernameActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}