package com.example.picster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.picster.model.Feed;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FriendPageActivity extends AppCompatActivity {
    BottomNavigationView navigationView;
    private ListView feedListView;
    private ArrayList<Feed> feedList;
    private ArrayAdapter<Feed> feedAdapter;
    String friendUsername, previousAct;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_page);

        friendUsername = getIntent().getStringExtra("friendUsername");
        previousAct = getIntent().getStringExtra("previousAct");

        navigationView = findViewById(R.id.bottom_navigation);
        if (previousAct.equals("dashboard")) {
            navigationView.setSelectedItemId(R.id.navigation_home);
        } else {
            navigationView.setSelectedItemId(R.id.navigation_bookmarks);
        }
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int selected = item.getItemId();

                if (selected == R.id.navigation_search) {
                    Intent searchIntent = new Intent(FriendPageActivity.this, SearchActivity.class);
                    startActivityForResult(searchIntent, 1);
                } else if (selected == R.id.navigation_bookmarks) {
                    Intent bookmarkIntent = new Intent(FriendPageActivity.this, BookmarkActivity.class);
                    startActivityForResult(bookmarkIntent, 2);
                } else if (selected == R.id.navigation_home) {
                    Intent homeIntent = new Intent(FriendPageActivity.this, DashboardActivity.class);
                    startActivityForResult(homeIntent, 3);
                } else if (selected == R.id.navigation_user) {
                    Intent myPageIntent = new Intent(FriendPageActivity.this, MyPageActivity.class);
                    startActivityForResult(myPageIntent, 4);
                } else if (selected == R.id.navigation_setting) {
                    Intent myPageIntent = new Intent(FriendPageActivity.this, SettingActivity.class);
                    startActivityForResult(myPageIntent, 4);
                }
                return true;
            }
        });

        ImageView backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (previousAct.equals("dashboard")) {
                    Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getApplicationContext(), BookmarkActivity.class);
                    startActivity(intent);
                }
            }
        });

        TextView userName = findViewById(R.id.userName);
        userName.setText(friendUsername);


        feedList = new ArrayList<>();
        feedAdapter = new FriendPageActivity.FeedAdapter(this, feedList);
        feedListView = findViewById(R.id.feedListView);
        feedListView.setAdapter(feedAdapter);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("Feed")
                .whereEqualTo("username", friendUsername)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        feedList.clear();

                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            Feed feed = document.toObject(Feed.class);
                            if (feed != null) {
                                feedList.add(feed);
                            }
                        }

                        feedAdapter.notifyDataSetChanged();

                    } else {
                        Toast.makeText(FriendPageActivity.this, "Failed to load feeds", Toast.LENGTH_SHORT).show();
                    }
                });
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