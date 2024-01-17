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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.picster.model.Feed;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookmarkActivity extends AppCompatActivity {
    BottomNavigationView navigationView;
    private FirebaseAuth mAuth;
    ArrayList<String> saved;
    List<Feed> feeds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore database = FirebaseFirestore.getInstance();


        navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setSelectedItemId(R.id.navigation_bookmarks);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int selected = item.getItemId();

                if (selected == R.id.navigation_search) {
                    Intent searchIntent = new Intent(BookmarkActivity.this, SearchActivity.class);
                    startActivityForResult(searchIntent, 1);
                } else if (selected == R.id.navigation_bookmarks) {
                    // stay
                } else if (selected == R.id.navigation_home) {
                    Intent homeIntent = new Intent(BookmarkActivity.this, DashboardActivity.class);
                    startActivityForResult(homeIntent, 3);
                } else if (selected == R.id.navigation_user) {
                    Intent myPageIntent = new Intent(BookmarkActivity.this, MyPageActivity.class);
                    startActivityForResult(myPageIntent, 4);
                } else if (selected == R.id.navigation_setting) {
                    Intent myPageIntent = new Intent(BookmarkActivity.this, SettingActivity.class);
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
                            saved = (ArrayList<String>) documentSnapshot.get("save");

                            database.collection("Feed")
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {

                                        feeds = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                            Feed feed = document.toObject(Feed.class);

                                            if (saved.contains(document.getId()) && !feed.getReported()) {
                                                feeds.add(feed);
                                            }
                                        }

                                        BookmarkActivity.FeedAdapter adapter = new BookmarkActivity.FeedAdapter(BookmarkActivity.this, R.layout.home_feed_list_item, feeds);
                                        ListView feedListView = findViewById(R.id.feedListView);
                                        feedListView.setAdapter(adapter);

                                        feedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                Feed clickedFeed = feeds.get(position);

                                                Intent intent = new Intent(BookmarkActivity.this, FriendFeedActivity.class);
                                                intent.putExtra("clickedFeed", clickedFeed);
                                                intent.putExtra("previousAct", "bookmark");
                                                startActivity(intent);
                                            }
                                        });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(BookmarkActivity.this, "Failed to retrieve Feed documents: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(BookmarkActivity.this, "Failed to retrieve friends list", Toast.LENGTH_SHORT).show();
                    });
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

            Glide.with(BookmarkActivity.this).load(currentFeed.getImageUri()).into(feedImage);
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