package com.example.picster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.picster.model.Feed;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.bumptech.glide.Glide;

public class MyPageActivity extends AppCompatActivity {
    BottomNavigationView navigationView;
    private FirebaseAuth mAuth;
    private CollectionReference feedCollection;
    private ListView feedListView;
    private List<Feed> feedList;
    private ArrayAdapter<Feed> feedAdapter;
    String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        feedCollection = database.collection("Feed");

        navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setSelectedItemId(R.id.navigation_user);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int selected = item.getItemId();

                if (selected == R.id.navigation_search) {
                    Intent searchIntent = new Intent(MyPageActivity.this, SearchActivity.class);
                    startActivityForResult(searchIntent, 1);
                } else if (selected == R.id.navigation_bookmarks) {
                    Intent bookmarkIntent = new Intent(MyPageActivity.this, BookmarkActivity.class);
                    startActivityForResult(bookmarkIntent, 2);
                } else if (selected == R.id.navigation_home) {
                    Intent homeIntent = new Intent(MyPageActivity.this, DashboardActivity.class);
                    startActivityForResult(homeIntent, 3);
                } else if (selected == R.id.navigation_user) {
                    // stay
                } else if (selected == R.id.navigation_setting) {
                    Intent myPageIntent = new Intent(MyPageActivity.this, SettingActivity.class);
                    startActivityForResult(myPageIntent, 4);
                }
                return true;
            }
        });

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            database.collection("User")
                    .document(userEmail)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            if (username != null) {
                                TextView usernameText = findViewById(R.id.userName);
                                user = username;
                                usernameText.setText(username);
                            } else {
                            }
                        } else {
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MyPageActivity.this, "Failed to retrieve username", Toast.LENGTH_SHORT).show();
                    });
        }
        ImageView uploadBtn = findViewById(R.id.uploadBtn);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UploadFeedActivity.class);
                startActivity(intent);
            }
        });

        feedList = new ArrayList<>();
        feedAdapter = new FeedAdapter(this, feedList);

        feedListView = findViewById(R.id.feedListView);
        feedListView.setAdapter(feedAdapter);

        feedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Feed selectedFeed = feedList.get(position);

                Intent intent = new Intent(MyPageActivity.this, MyFeedActivity.class);
                intent.putExtra("selectedFeed", selectedFeed);
                intent.putExtra("username", user);
                startActivity(intent);
            }
        });

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            feedCollection
                    .whereGreaterThanOrEqualTo("id", userEmail)
                    .whereLessThanOrEqualTo("id", userEmail + "\uf8ff")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            feedList.clear();

                            for (DocumentSnapshot document : task.getResult().getDocuments()) {
                                Feed feed = document.toObject(Feed.class);
                                if (feed != null && !feed.getReported()) {
                                    feedList.add(feed);
                                }
                            }

                            feedAdapter.notifyDataSetChanged();

                        } else {
                            Toast.makeText(MyPageActivity.this, "Failed to load feeds", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private class FeedAdapter extends ArrayAdapter<Feed> {
        private LayoutInflater inflater;

        public FeedAdapter(Context context, List<Feed> feedList) {
            super(context, 0, feedList);
            inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, @NonNull View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.one_feed_list_item, parent, false);
            }

            ImageView feedImageView = view.findViewById(R.id.feedImage);
            TextView feedText = view.findViewById(R.id.feedText);
            TextView likeNumber = view.findViewById(R.id.likeNumber);
            TextView commentNumber = view.findViewById(R.id.commentNumber);
            TextView feedDate = view.findViewById(R.id.feedDate);

            Feed feed = getItem(position);

            if (feed != null) {
                Glide.with(getContext())
                        .load(feed.getImageUri())
                        .into(feedImageView);

                feedText.setText(feed.getContent());
                likeNumber.setText(String.valueOf(feed.getLikes()));
                commentNumber.setText(String.valueOf(feed.getComments().size()));

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
                String formattedDate = sdf.format(new Date(Long.parseLong(feed.getDate())));
                feedDate.setText(formattedDate);
            }

            return view;
        }
    }
}