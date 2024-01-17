package com.example.picster;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import com.example.picster.model.Comment;
import com.example.picster.model.Feed;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
//import com.squareup.picasso.Picasso;

import java.io.IOException;

public class UploadFeedActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    ImageView feedImage;
    ProgressBar progressBar;
    EditText contentText;
    private StorageReference storageRef;
    BottomNavigationView navigationView;
    String usernameMain;
    String userEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_feed);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        if (user != null) {
            userEmail = user.getEmail();

            database.collection("User")
                    .document(userEmail)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            if (username != null) {
                                TextView usernameText = findViewById(R.id.userName);
                                usernameMain = username;
                                usernameText.setText(username);
                            } else {
                            }
                        } else {
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(UploadFeedActivity.this, "Failed to retrieve username", Toast.LENGTH_SHORT).show();
                    });
        }

        navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setSelectedItemId(R.id.navigation_user);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int selected = item.getItemId();

                if (selected == R.id.navigation_search) {
                    Intent searchIntent = new Intent(UploadFeedActivity.this, SearchActivity.class);
                    startActivityForResult(searchIntent, 1);
                } else if (selected == R.id.navigation_bookmarks) {
                    Intent homeIntent = new Intent(UploadFeedActivity.this, BookmarkActivity.class);
                    startActivityForResult(homeIntent, 2);
                } else if (selected == R.id.navigation_home) {
                    Intent homeIntent = new Intent(UploadFeedActivity.this, DashboardActivity.class);
                    startActivityForResult(homeIntent, 3);
                } else if (selected == R.id.navigation_user) {
                    Intent myPageIntent = new Intent(UploadFeedActivity.this, MyPageActivity.class);
                    startActivityForResult(myPageIntent, 4);
                } else if (selected == R.id.navigation_setting) {
                    Intent myPageIntent = new Intent(UploadFeedActivity.this, SettingActivity.class);
                    startActivityForResult(myPageIntent, 5);
                }
                return true;
            }
        });


        storageRef = FirebaseStorage.getInstance().getReference("feed");

        Button selectBtn = findViewById(R.id.selectBtn);
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChooser();
            }
        });

        feedImage = findViewById(R.id.feedImage);

        contentText = findViewById(R.id.contentText);

        progressBar = findViewById(R.id.progressBar);

        Button uploadBtn = findViewById(R.id.uploadBtn);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String email = contentText.getText().toString();
                upload();
            }
        });

        Button cancelBtn = findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyPageActivity.class);
                startActivity(intent);
            }
        });
    }

    private void openChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    private void upload() {
        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(0);
                                }
                            }, 5000);
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri downloadUri) {
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    if (user != null) {
                                        String username = usernameMain;

                                        String content = contentText.getText().toString();
                                        boolean isPublic = false;
                                        List<Comment> comments = new ArrayList<>();

                                        String newFeedRef = userEmail+String.valueOf(System.currentTimeMillis());
                                        Feed feed = new Feed(
                                                newFeedRef,
                                                username,
                                                String.valueOf(System.currentTimeMillis()),
                                                downloadUri.toString(),
                                                content,
                                                0,
                                                isPublic,
                                                comments,
                                                false
                                        );
                                        FirebaseFirestore database = FirebaseFirestore.getInstance();

                                        database.collection("Feed").document(newFeedRef).set(feed)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(UploadFeedActivity.this, "Added feed info.", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(getApplicationContext(), MyPageActivity.class);
                                                        startActivity(intent);
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(UploadFeedActivity.this, "Failed to add feed info.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(UploadFeedActivity.this, "User not logged in.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UploadFeedActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());

                            progressBar.setProgress((int) progress);
                        }
                    });
        } else {
            Toast.makeText(this, "No image is selected.", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            //Picasso.with(this).load(imageUri).into(feedImage);
            feedImage.setImageURI(imageUri);
        }
    }
}