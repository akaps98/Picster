package com.example.picster;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.picster.model.Feed;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminActivity extends AppCompatActivity {
    List<Feed> feeds;
    Button logoutButton;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        logoutButton = findViewById(R.id.logoutButton);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        CollectionReference feedRef = FirebaseFirestore.getInstance().collection("Feed");

        feedRef.whereEqualTo("reported", true).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    feeds = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Feed feed = document.toObject(Feed.class);
                        feeds.add(feed);
                    }

                    FeedAdapter feedAdapter = new FeedAdapter(AdminActivity.this, R.layout.home_feed_list_item, feeds);
                    ListView adminFeedListView = findViewById(R.id.adminFeedListView);
                    adminFeedListView.setAdapter(feedAdapter);

                    adminFeedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Feed clickedFeed = feeds.get(position);

                            Intent intent = new Intent(AdminActivity.this, AdminFeedActivity.class);
                            intent.putExtra("clickedFeed", clickedFeed);
                            startActivity(intent);
                        }
                    });
                }
            }
        });

        FirebaseUser user = auth.getCurrentUser();

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutButton.animate().alpha(0.2f).setDuration(150).withEndAction(() -> {
                    logoutButton.animate().alpha(1.0f).setDuration(150).start();
                }).start();

                if (user != null) {
                    auth.signOut();
                    mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(AdminActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AdminActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(AdminActivity.this, "Logout failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AdminActivity.this, "Failed to sign out;" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(AdminActivity.this, "Please log in first", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private class FeedAdapter extends ArrayAdapter<Feed> {

        public FeedAdapter(Context context, int resource, List<Feed> feeds) {
            super(context, resource, feeds);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.admin_feed_list_item, parent, false);
            }

            Feed currentFeed = getItem(position);

            ImageView feedImage = convertView.findViewById(R.id.feedImage);
            TextView feedUser = convertView.findViewById(R.id.feedUser);
            TextView feedText = convertView.findViewById(R.id.feedText);
            TextView feedDate = convertView.findViewById(R.id.feedDate);
            Button deletePostButton = convertView.findViewById(R.id.deletePostButton);
            Button freePostButton = convertView.findViewById(R.id.freePostButton);

            Glide.with(AdminActivity.this).load(currentFeed.getImageUri()).into(feedImage);
            feedUser.setText(currentFeed.getUsername());
            feedText.setText(currentFeed.getContent());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
            String formattedDate = sdf.format(new Date(Long.parseLong(currentFeed.getDate())));
            feedDate.setText(formattedDate);

            deletePostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference feedRef = db.collection("Feed");

                    feedRef.document(currentFeed.getId()).delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getContext(), "Feed has been deleted!", Toast.LENGTH_SHORT).show();

                                    Intent intent = getIntent();
                                    finish();
                                    startActivity(intent);
                                }
                            });
                }
            });

            freePostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference feedRef = db.collection("Feed");

                    feedRef.document(currentFeed.getId()).update("reported", false)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getContext(), "Report has been cancelled!", Toast.LENGTH_SHORT).show();

                                    Intent intent = getIntent();
                                    finish();
                                    startActivity(intent);
                                }
                            });
                }
            });

            return convertView;
        }
    }
}