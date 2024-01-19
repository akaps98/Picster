package com.example.picster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.picster.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FriendListActivity extends AppCompatActivity {
    BottomNavigationView navigationView;
    String userEmail;

    List<String> friendEmail, friendUsername;
    FirebaseFirestore firestore;

    DocumentReference userDocument;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setSelectedItemId(R.id.navigation_setting);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int selected = item.getItemId();

                if (selected == R.id.navigation_search) {
                    Intent searchIntent = new Intent(FriendListActivity.this, SearchActivity.class);
                    startActivityForResult(searchIntent, 1);
                } else if (selected == R.id.navigation_bookmarks) {
                    Intent bookmarkIntent = new Intent(FriendListActivity.this, BookmarkActivity.class);
                    startActivityForResult(bookmarkIntent, 2);
                } else if (selected == R.id.navigation_home) {
                    Intent homeIntent = new Intent(FriendListActivity.this, DashboardActivity.class);
                    startActivityForResult(homeIntent, 3);
                } else if (selected == R.id.navigation_user) {
                    Intent myPageIntent = new Intent(FriendListActivity.this, MyPageActivity.class);
                    startActivityForResult(myPageIntent, 4);
                } else if (selected == R.id.navigation_setting) {
                    Intent myPageIntent = new Intent(FriendListActivity.this, SettingActivity.class);
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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userEmail = currentUser.getEmail();
        firestore = FirebaseFirestore.getInstance();
        userDocument = firestore.collection("User").document(userEmail);

        fetchUsernames();
    }

    private void fetchUsernames() {
        userDocument.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                friendEmail = (List<String>) documentSnapshot.get("friends");

                friendUsername = new ArrayList<>();
                for (String email : friendEmail) {
                    DocumentReference friendDocument = firestore.collection("User").document(email);

                    friendDocument.get().addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String username = document.getString("username");
                            friendUsername.add(username);

                            if (friendUsername.size() == friendEmail.size()) {
                                ListView friendListView = findViewById(R.id.friendListView);

                                friendListView.setOnItemClickListener((parent, view, position, id) -> {
                                    String selectedFriendUsername = friendUsername.get(position);

                                    Intent friendPageIntent = new Intent(FriendListActivity.this, FriendPageActivity.class);
                                    friendPageIntent.putExtra("friendUsername", selectedFriendUsername);
                                    friendPageIntent.putExtra("previousAct", "myFriendList");
                                    startActivity(friendPageIntent);
                                });
                                ArrayAdapter<String> friendAdapter = new ArrayAdapter<String>(this, R.layout.my_friend_list_item, R.id.userName, friendUsername) {
                                    @NonNull
                                    @Override
                                    public View getView(int position, @NonNull View convertView, @NonNull ViewGroup parent) {
                                        View view = super.getView(position, convertView, parent);

                                        ImageView removeFriendBtn = view.findViewById(R.id.removeFriendBtn);
                                        removeFriendBtn.setOnClickListener(v -> {
                                            String friendToRemove = friendEmail.get(position);
                                            removeFriend(friendToRemove);
                                        });

                                        return view;
                                    }
                                };
                                friendListView.setAdapter(friendAdapter);
                            }
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(FriendListActivity.this, "Failed to fetch friend usernames", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(FriendListActivity.this, "Failed to fetch friend list", Toast.LENGTH_SHORT).show();
        });
    }

    private void removeFriend(String friendEmailToRemove) {
        friendEmail.remove(friendEmailToRemove);

        userDocument.update("friends", friendEmail)
                .addOnSuccessListener(aVoid -> {
                    fetchUsernames();
                    Toast.makeText(FriendListActivity.this, "Friend removed successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(FriendListActivity.this, "Failed to remove friend", Toast.LENGTH_SHORT).show());
    }
}