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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.picster.model.Comment;
import com.example.picster.model.Feed;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FriendFeedActivity extends AppCompatActivity {
    BottomNavigationView navigationView;
    private FirebaseAuth mAuth;
    ArrayList<String> saved;
    ArrayList<String> liked;
    String userEmail;
    String username;
    TextView likeNumber, commentNumber;
    Feed feed;
    List<Comment> comments;
    String previousAct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_feed);

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        feed = (Feed) getIntent().getSerializableExtra("clickedFeed");
        previousAct = getIntent().getStringExtra("previousAct");

        TextView feedUserName = findViewById(R.id.feedUserName);
        TextView feedDate = findViewById(R.id.feedDate);
        ImageView feedPicture = findViewById(R.id.feedPicture);
        TextView feedText = findViewById(R.id.feedText);
        likeNumber = findViewById(R.id.likeNumber);
        commentNumber = findViewById(R.id.commentNumber);
        comments = feed.getComments();

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


        ListView commentListView = findViewById(R.id.commentList);
        FriendFeedActivity.CommentAdapter commentAdapter = new FriendFeedActivity.CommentAdapter(this, comments);
        commentListView.setAdapter(commentAdapter);

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


        ImageView saveBtn = findViewById(R.id.saveImage);
        ImageView likeBtn = findViewById(R.id.likeImage);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();
            database.collection("User")
                    .document(userEmail)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            username = (String) documentSnapshot.get("username");
                            saved = (ArrayList<String>) documentSnapshot.get("save");
                            liked = (ArrayList<String>) documentSnapshot.get("like");

                            if (liked.contains(feed.getId())) {
                                likeBtn.setImageResource(R.drawable.heart_fill);
                            }
                            if (saved.contains(feed.getId())) {
                                saveBtn.setImageResource(R.drawable.save_fill);
                            }
                        } else {
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(FriendFeedActivity.this, "Failed to retrieve lists", Toast.LENGTH_SHORT).show();
                    });
        }

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (saved.contains(feed.getId())) {
                    saved.remove(feed.getId());
                    saveBtn.setImageResource(R.drawable.save_empty);
                } else {
                    saved.add(feed.getId());
                    saveBtn.setImageResource(R.drawable.save_fill);
                }

                database.collection("User")
                        .document(userEmail)
                        .update("save", saved)
                        .addOnSuccessListener(aVoid -> {
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(FriendFeedActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                        });
            }
        });
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (liked.contains(feed.getId())) {
                    liked.remove(feed.getId());
                    likeBtn.setImageResource(R.drawable.heart_empty);
                    int currentLikes = feed.getLikes();
                    currentLikes--;
                    feed.setLikes(currentLikes);
                } else {
                    liked.add(feed.getId());
                    likeBtn.setImageResource(R.drawable.heart_fill);
                    int currentLikes = feed.getLikes();
                    currentLikes++;
                    feed.setLikes(currentLikes);
                }
                likeNumber.setText(String.valueOf(feed.getLikes()));

                database.collection("User")
                        .document(userEmail)
                        .update("like", liked)
                        .addOnSuccessListener(aVoid -> {
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(FriendFeedActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                        });
                database.collection("Feed")
                        .document(feed.getId())
                        .update("likes", feed.getLikes())
                        .addOnSuccessListener(aVoid -> {
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(FriendFeedActivity.this, "Failed to update likes count", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredComment = commentNew.getText().toString();
                if (!enteredComment.equals("")) {
                    Comment newComment = new Comment(username, enteredComment);
                    addCommentToFeed(feed.getId(), newComment);
                } else {
                    Toast.makeText(FriendFeedActivity.this, "Enter comment text.", Toast.LENGTH_LONG).show();
                }
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

        DocumentReference feedDocument = db.collection("Feed").document(feedId);

        feedDocument.update("comments", FieldValue.arrayUnion(newComment))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(FriendFeedActivity.this, "Comment added successfully", Toast.LENGTH_SHORT).show();
                        int newNum = feed.getComments().size() + 1;
                        commentNumber.setText(String.valueOf(newNum));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
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