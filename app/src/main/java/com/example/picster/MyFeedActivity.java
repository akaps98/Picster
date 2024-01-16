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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.firestore.FieldValue;
import com.bumptech.glide.Glide;
import com.example.picster.model.Comment;
import com.example.picster.model.Feed;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyFeedActivity extends AppCompatActivity {
    BottomNavigationView navigationView;
    Feed feed;
    private FirebaseAuth mAuth;
    String username;
    List<Comment> comments;
    ArrayList<String> liked;
    String userEmail;
    TextView likes, commentNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_feed);

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setSelectedItemId(R.id.navigation_user);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int selected = item.getItemId();

                if (selected == R.id.navigation_search) {
                    Intent searchIntent = new Intent(MyFeedActivity.this, SearchActivity.class);
                    startActivityForResult(searchIntent, 1);
                } else if (selected == R.id.navigation_bookmarks) {
                    Intent bookmarkIntent = new Intent(MyFeedActivity.this, BookmarkActivity.class);
                    startActivityForResult(bookmarkIntent, 2);
                } else if (selected == R.id.navigation_home) {
                    Intent homeIntent = new Intent(MyFeedActivity.this, DashboardActivity.class);
                    startActivityForResult(homeIntent, 3);
                } else if (selected == R.id.navigation_user) {
                    Intent myPageIntent = new Intent(MyFeedActivity.this, MyPageActivity.class);
                    startActivityForResult(myPageIntent, 4);
                } else if (selected == R.id.navigation_setting) {
                    Intent myPageIntent = new Intent(MyFeedActivity.this, SettingActivity.class);
                    startActivityForResult(myPageIntent, 5);
                }
                return true;
            }
        });

        ImageView backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyPageActivity.class);
                startActivity(intent);
            }
        });

        ImageView deleteBtn = findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MyFeedActivity.this);
                builder.setMessage("Are you sure you want to delete this feed?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteFeed(feed.getId());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });

        ImageView editBtn = findViewById(R.id.editBtn);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MyFeedActivity.this);
                builder.setMessage("Do you wish to make this feed public?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (isVipUser()) {
                                    feed.setPublic(true);
                                    Toast.makeText(MyFeedActivity.this, "This feed is now set to public.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Intent paymentIntent = new Intent(MyFeedActivity.this, PaymentActivity.class);
                                    startActivity(paymentIntent);
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });

        Intent intent = getIntent();
        feed = (Feed) getIntent().getSerializableExtra("selectedFeed");
        username = intent.getStringExtra("username");

        TextView usernameTextView = findViewById(R.id.userName);
        usernameTextView.setText(username);
        TextView date = findViewById(R.id.feedDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        String formattedDate = sdf.format(new Date(Long.parseLong(feed.getDate())));
        date.setText(formattedDate);
        ImageView feedImage = findViewById(R.id.feedPicture);
        Glide.with(this).load(feed.getImageUri()).into(feedImage);

        TextView feedText = findViewById(R.id.feedText);
        feedText.setText(feed.getContent());
        likes = findViewById(R.id.likeNumber);
        likes.setText(String.valueOf(feed.getLikes()));
        commentNum = findViewById(R.id.commentNumber);
        commentNum.setText(String.valueOf(feed.getComments().size()));

        comments = feed.getComments();

        ImageView likeBtn = findViewById(R.id.likeImage);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();
            database.collection("User")
                    .document(userEmail)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            liked = (ArrayList<String>) documentSnapshot.get("like");

                            if (liked.contains(feed.getId())) {
                                likeBtn.setImageResource(R.drawable.heart_fill);
                            }
                        } else {
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MyFeedActivity.this, "Failed to retrieve lists", Toast.LENGTH_SHORT).show();
                    });
        }

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
                likes.setText(String.valueOf(feed.getLikes()));

                database.collection("User")
                        .document(userEmail)
                        .update("like", liked)
                        .addOnSuccessListener(aVoid -> {
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MyFeedActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                        });
                database.collection("Feed")
                        .document(feed.getId())
                        .update("likes", feed.getLikes())
                        .addOnSuccessListener(aVoid -> {
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MyFeedActivity.this, "Failed to update likes count", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        ListView commentListView = findViewById(R.id.commentList);
        CommentAdapter commentAdapter = new CommentAdapter(this, comments);
        commentListView.setAdapter(commentAdapter);


        EditText commentNew = findViewById(R.id.editTextComment);
        ImageView commentBtn = findViewById(R.id.commentBtn);
        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredComment = commentNew.getText().toString();
                if (!enteredComment.equals("")) {
                    Comment newComment = new Comment(username, enteredComment);
                    addCommentToFeed(feed.getId(), newComment);
                } else {
                    Toast.makeText(MyFeedActivity.this, "Enter comment text.", Toast.LENGTH_LONG).show();
                }
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
                        Toast.makeText(MyFeedActivity.this, "Comment added successfully", Toast.LENGTH_SHORT).show();
                        int newNum = feed.getComments().size() + 1;
                        commentNum.setText(String.valueOf(newNum));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MyFeedActivity.this, "Error adding comment", Toast.LENGTH_SHORT).show();
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

    private void deleteFeed(String feedId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Feed")
                .whereEqualTo("id", feedId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                document.getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(MyFeedActivity.this, "Feed deleted successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(MyFeedActivity.this, MyPageActivity.class);
                                        startActivity(intent);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MyFeedActivity.this, "Error deleting feed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(MyFeedActivity.this, "Error finding feed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean isVipUser() {
        Boolean isVip = false; //default value

        // get vip field of the user

        return isVip;
    }
}