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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.picster.model.Comment;
import com.example.picster.model.Feed;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FriendFeedActivity extends AppCompatActivity {
    BottomNavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_feed);

        Feed feed = (Feed) getIntent().getSerializableExtra("clickedFeed");

        TextView feedUserName = findViewById(R.id.feedUserName);
        TextView feedDate = findViewById(R.id.feedDate);
        ImageView feedPicture = findViewById(R.id.feedPicture);
        TextView feedText = findViewById(R.id.feedText);
        TextView likeNumber = findViewById(R.id.likeNumber);
        TextView commentNumber = findViewById(R.id.commentNumber);

        feedUserName.setText(feed.getUsername());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        String formattedDate = sdf.format(new Date(Long.parseLong(feed.getDate())));
        feedDate.setText(formattedDate);
        Glide.with(this).load(feed.getImageUri()).into(feedPicture);
        feedText.setText(feed.getContent());
        likeNumber.setText(String.valueOf(feed.getLikes()));
        commentNumber.setText(String.valueOf(feed.getComments().size()));

        EditText commentNew = findViewById(R.id.editTextComment);
        ImageView commentBtn = findViewById(R.id.commentBtn);
        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredComment = commentNew.getText().toString();
                if (!enteredComment.equals("")) {
                    Comment newComment = new Comment(feed.getUsername(), enteredComment);
                    addCommentToFeed(feed.getId(), newComment);
                } else {
                    Toast.makeText(FriendFeedActivity.this, "Enter comment text.", Toast.LENGTH_LONG).show();
                }
            }
        });


        ImageView backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                startActivity(intent);
            }
        });
        navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setSelectedItemId(R.id.navigation_home);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int selected = item.getItemId();

                if (selected == R.id.navigation_search) {
                    Intent searchIntent = new Intent(FriendFeedActivity.this, SearchActivity.class);
                    startActivityForResult(searchIntent, 1);
                } else if (selected == R.id.navigation_bookmarks) {
                    Intent bookmarkIntent = new Intent(FriendFeedActivity.this, BookmarkActivity.class);
                    startActivityForResult(bookmarkIntent, 2);
                } else if (selected == R.id.navigation_home) {
                    Intent homeIntent = new Intent(FriendFeedActivity.this, DashboardActivity.class);
                    startActivityForResult(homeIntent, 3);
                } else if (selected == R.id.navigation_user) {
                    Intent myPageIntent = new Intent(FriendFeedActivity.this, MyPageActivity.class);
                    startActivityForResult(myPageIntent, 4);
                } else if (selected == R.id.navigation_setting) {
                    Intent myPageIntent = new Intent(FriendFeedActivity.this, SettingActivity.class);
                    startActivityForResult(myPageIntent, 5);
                }
                return true;
            }
        });
    }

    private void addCommentToFeed(String feedId, Comment newComment) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the document in the "Feed" collection
        DocumentReference feedDocument = db.collection("Feed").document(feedId);

        // Update the 'comments' field with the new comment
        feedDocument.update("comments", FieldValue.arrayUnion(newComment))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Comment added successfully
                        Toast.makeText(FriendFeedActivity.this, "Comment added successfully", Toast.LENGTH_SHORT).show();

                        // Refresh the comments list or perform any other necessary actions
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Toast.makeText(FriendFeedActivity.this, "Error adding comment", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private class CommentAdapter extends ArrayAdapter<Comment> {
        public CommentAdapter(Context context, List<Comment> comments) {
            super(context, 0, comments);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Comment comment = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment_list_item, parent, false);
            }

            TextView usernameTextView = convertView.findViewById(R.id.commentUsername);
            TextView commentTextView = convertView.findViewById(R.id.commentText);

            if (comment != null) {
                usernameTextView.setText(comment.getUsername());
                commentTextView.setText(comment.getComment());
            }

            return convertView;
        }
    }
}