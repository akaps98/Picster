package com.example.picster;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.picster.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    BottomNavigationView navigationView;
    ImageButton searchButton;
    RadioGroup searchOptions;
    SearchView searchView;
    ListView searchListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setSelectedItemId(R.id.navigation_search);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int selected = item.getItemId();

                if (selected == R.id.navigation_search) {
                    // stay
                } else if (selected == R.id.navigation_bookmarks) {
                    Intent bookmarkIntent = new Intent(SearchActivity.this, BookmarkActivity.class);
                    startActivityForResult(bookmarkIntent, 2);
                } else if (selected == R.id.navigation_home) {
                    Intent homeIntent = new Intent(SearchActivity.this, DashboardActivity.class);
                    startActivityForResult(homeIntent, 3);
                } else if (selected == R.id.navigation_user) {
                    Intent myPageIntent = new Intent(SearchActivity.this, MyPageActivity.class);
                    startActivityForResult(myPageIntent, 4);
                } else if (selected == R.id.navigation_setting) {
                    Intent myPageIntent = new Intent(SearchActivity.this, SettingActivity.class);
                    startActivityForResult(myPageIntent, 4);
                }
                return true;
            }
        });

        searchButton = findViewById(R.id.searchButton);
        searchOptions = findViewById(R.id.searchOptions);
        searchView = findViewById(R.id.searchView);
        searchListView = findViewById(R.id.searchListView);

        FirebaseFirestore database = FirebaseFirestore.getInstance();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedRadioButtonId = searchOptions.getCheckedRadioButtonId();

                String searchText = searchView.getQuery().toString().trim();

                if (selectedRadioButtonId == -1) {
                    Toast.makeText(SearchActivity.this, "Please select an option!", Toast.LENGTH_SHORT).show();
                } else if (searchText.isEmpty()) {
                    Toast.makeText(SearchActivity.this, "Please enter search value!", Toast.LENGTH_SHORT).show();
                } else {
                    RadioButton selected = findViewById(selectedRadioButtonId);

                    String selectedOption = selected.getText().toString();

                    if (selectedOption.equals("Friend")) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        FirebaseUser currentUser = auth.getCurrentUser();

                        if (currentUser != null) {
                            db.collection("User")
                                    .whereGreaterThanOrEqualTo("username", searchText)
                                    .whereLessThanOrEqualTo("username", searchText + "\uf8ff")
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            List<User> userList = new ArrayList<>();

                                            for (DocumentSnapshot document : task.getResult()) {
                                                User searchedUser = document.toObject(User.class);
                                                if (searchedUser != null && !searchedUser.getEmail().equals(currentUser.getEmail())) {
                                                    userList.add(searchedUser);
                                                }
                                            }

                                            UserListAdapter userListAdapter = new UserListAdapter(SearchActivity.this, userList);
                                            searchListView.setAdapter(userListAdapter);
                                        } else {
                                            Toast.makeText(SearchActivity.this, "Error fetching user data.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                }
            }
        });
    }

    public class UserListAdapter extends ArrayAdapter<User> {
        private final Context context;
        private final List<User> userList;

        public UserListAdapter(Context context, List<User> userList) {
            super(context, 0, userList);
            this.context = context;
            this.userList = userList;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = LayoutInflater.from(context).inflate(R.layout.friend_list_item, parent, false);
            }

            User currentUser = userList.get(position);

            ImageView userImage = itemView.findViewById(R.id.userMainImage);
            TextView searchUserName = itemView.findViewById(R.id.searchUserName);
            ImageView addFriendBtn = itemView.findViewById(R.id.addFriendBtn);

            searchUserName.setText(currentUser.getUsername());

            addFriendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    FirebaseUser loggedInUser = auth.getCurrentUser();

                    if (loggedInUser != null) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        db.collection("User")
                                .document(loggedInUser.getEmail())
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            User loggedInUserInfo = document.toObject(User.class);

                                            if (loggedInUserInfo != null) {
                                                List<String> friendsList = loggedInUserInfo.getFriends();
                                                if (friendsList == null) {
                                                    friendsList = new ArrayList<>();
                                                }

                                                if (!friendsList.contains(currentUser.getEmail())) {
                                                    friendsList.add(currentUser.getEmail());

                                                    db.collection("User")
                                                            .document(loggedInUser.getEmail())
                                                            .update("friends", friendsList)
                                                            .addOnCompleteListener(updateTask -> {
                                                                if (updateTask.isSuccessful()) {
                                                                    Toast.makeText(context, "Friend added successfully!", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                } else {
                                                    Toast.makeText(context, "Already added as a friend!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        } else {
                                            Toast.makeText(context, "User is not found.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(context, "Error fetching user data.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            });

            return itemView;
        }
    }
}