package com.example.picster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ChangeUsernameActivity extends AppCompatActivity {
    BottomNavigationView navigationView;
    EditText changedUsername;
    Button changeUsernameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_username);

        navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setSelectedItemId(R.id.navigation_setting);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int selected = item.getItemId();

                if (selected == R.id.navigation_search) {
                    Intent searchIntent = new Intent(ChangeUsernameActivity.this, SearchActivity.class);
                    startActivityForResult(searchIntent, 1);
                } else if (selected == R.id.navigation_bookmarks) {
                    Intent bookmarkIntent = new Intent(ChangeUsernameActivity.this, BookmarkActivity.class);
                    startActivityForResult(bookmarkIntent, 2);
                } else if (selected == R.id.navigation_home) {
                    Intent homeIntent = new Intent(ChangeUsernameActivity.this, DashboardActivity.class);
                    startActivityForResult(homeIntent, 3);
                } else if (selected == R.id.navigation_user) {
                    Intent myPageIntent = new Intent(ChangeUsernameActivity.this, MyPageActivity.class);
                    startActivityForResult(myPageIntent, 4);
                } else if (selected == R.id.navigation_setting) {
                    Intent myPageIntent = new Intent(ChangeUsernameActivity.this, SettingActivity.class);
                    startActivityForResult(myPageIntent, 4);
                }
                return true;
            }
        });

        ImageView backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(intent);
            }
        });

        changedUsername = findViewById(R.id.changedUsername);
        changeUsernameButton = findViewById(R.id.changeUsernameButton);

        changeUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = changedUsername.getText().toString().trim();
                if (username.isEmpty()) {
                    Toast.makeText(ChangeUsernameActivity.this, "Please input new username", Toast.LENGTH_SHORT).show();
                } else {
                    CollectionReference userRef = FirebaseFirestore.getInstance().collection("User");
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if(user != null) {
                    String email = user.getEmail();

                    userRef.whereEqualTo("username", username)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        QuerySnapshot querySnapshot = task.getResult();
                                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                            Toast.makeText(ChangeUsernameActivity.this, "Already exists username", Toast.LENGTH_SHORT).show();
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
                                                                                    Toast.makeText(ChangeUsernameActivity.this, "Updated!", Toast.LENGTH_SHORT).show();
                                                                                    finish();
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Toast.makeText(ChangeUsernameActivity.this, "Failed to update; " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        }
                                                    });
                                        }
                                    } else {
                                        Toast.makeText(ChangeUsernameActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    } else {
                        Toast.makeText(ChangeUsernameActivity.this, "Please log in first", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}