package com.example.picster;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.picster.model.Feed;
import com.example.picster.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchButton.animate().alpha(0.2f).setDuration(150).withEndAction(() -> {
                    searchButton.animate().alpha(1.0f).setDuration(150).start();
                }).start();

                int selectedRadioButtonId = searchOptions.getCheckedRadioButtonId();

                String searchText = searchView.getQuery().toString().trim();

                if (selectedRadioButtonId == -1) {
                    Toast.makeText(SearchActivity.this, "Please select an option!", Toast.LENGTH_SHORT).show();
                } else if (searchText.isEmpty()) {
                    Toast.makeText(SearchActivity.this, "Please enter search value!", Toast.LENGTH_SHORT).show();
                } else {
                    RadioButton selected = findViewById(selectedRadioButtonId);

                    String selectedOption = selected.getText().toString();
                    String lowerCaseSearchText = searchText.toLowerCase(Locale.getDefault());

                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    FirebaseUser currentUser = auth.getCurrentUser();

                    if (selectedOption.equals("Friend")) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        if (currentUser != null) {
                            db.collection("User")
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            List<User> searchedUserList = new ArrayList<>();

                                            for (DocumentSnapshot document : task.getResult()) {
                                                User searchedUser = document.toObject(User.class);
                                                if (searchedUser != null && !searchedUser.getEmail().equals(currentUser.getEmail())) {
                                                    if (searchedUser.getUsername().toLowerCase().contains(lowerCaseSearchText)) {
                                                        searchedUserList.add(searchedUser);
                                                    }
                                                }
                                            }

                                            if (searchedUserList.isEmpty()) {
                                                Toast.makeText(SearchActivity.this, "No users found!", Toast.LENGTH_SHORT).show();
                                                searchListView.setAdapter(null);
                                            } else {
                                                UserListAdapter userListAdapter = new UserListAdapter(SearchActivity.this, searchedUserList);
                                                searchListView.setAdapter(userListAdapter);
                                            }
                                        } else {
                                            Toast.makeText(SearchActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        List<Feed> feeds = new ArrayList<>();

                        searchListView.setAdapter(null);

                        db.collection("Feed")
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                        Feed feed = document.toObject(Feed.class);

                                        boolean matchesSearchText = feed.getContent().toLowerCase().contains(lowerCaseSearchText);

                                        List<String> friendsList = new ArrayList<>();

                                        if (matchesSearchText) {
                                            DocumentReference userRef = db.collection("User").document(currentUser.getEmail());

                                            userRef.get().addOnSuccessListener(documentSnapshot -> {
                                                if (documentSnapshot.exists()) {
                                                    User loggedInUserInfo = documentSnapshot.toObject(User.class);
                                                    if (loggedInUserInfo != null) {
                                                        List<String> fetchedFriendsList = loggedInUserInfo.getFriends();
                                                        if (fetchedFriendsList != null) {
                                                            friendsList.addAll(fetchedFriendsList);

                                                            if(!currentUser.getEmail().equals(feed.getId().split("\\d+")[0])) {
                                                                if (friendsList.contains(feed.getId().split("\\d+")[0]) || feed.isPublic()) {
                                                                    feeds.add(feed);
                                                                }
                                                            }

                                                        }
                                                    }
                                                }
                                                if (!feeds.isEmpty()) {
                                                    FeedAdapter adapter = new FeedAdapter(SearchActivity.this, R.layout.home_feed_list_item, feeds);
                                                    searchListView.setAdapter(adapter);

                                                    searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                        @Override
                                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                            Feed clickedFeed = feeds.get(position);

                                                            Intent intent = new Intent(SearchActivity.this, FriendFeedActivity.class);
                                                            intent.putExtra("clickedFeed", clickedFeed);
                                                            startActivity(intent);
                                                        }
                                                    });
                                                }
                                            }).addOnFailureListener(e -> {
                                                Toast.makeText(SearchActivity.this, "Error.", Toast.LENGTH_SHORT).show();
                                            });
                                        }
                                    }

                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(SearchActivity.this, "Error; " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
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

    private class FeedAdapter extends ArrayAdapter<Feed> {

        public FeedAdapter(Context context, int resource, List<Feed> feeds) {
            super(context, resource, feeds);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.home_feed_list_item, parent, false);
            }

            Feed currentFeed = getItem(position);

            ImageView feedImage = convertView.findViewById(R.id.feedImage);
            TextView feedUser = convertView.findViewById(R.id.feedUser);
            TextView feedText = convertView.findViewById(R.id.feedText);
            TextView likeNumber = convertView.findViewById(R.id.likeNumber);
            TextView commentNumber = convertView.findViewById(R.id.commentNumber);
            TextView feedDate = convertView.findViewById(R.id.feedDate);

            Glide.with(SearchActivity.this).load(currentFeed.getImageUri()).into(feedImage);
            feedUser.setText(currentFeed.getUsername());
            feedText.setText(currentFeed.getContent());
            likeNumber.setText(String.valueOf(currentFeed.getLikes()));
            commentNumber.setText(String.valueOf(currentFeed.getComments().size()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
            String formattedDate = sdf.format(new Date(Long.parseLong(currentFeed.getDate())));
            feedDate.setText(formattedDate);

            return convertView;
        }
    }
}