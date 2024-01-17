package com.example.picster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.picster.model.Feed;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminFeedActivity extends AppCompatActivity {
    Feed feed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_feed);

        ImageView backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AdminActivity.class);
                startActivity(intent);
            }
        });

        TextView feedUserName = findViewById(R.id.feedUserName);
        TextView feedDate = findViewById(R.id.feedDate);
        ImageView feedPicture = findViewById(R.id.feedPicture);
        TextView feedText = findViewById(R.id.feedText);
        feed = (Feed) getIntent().getSerializableExtra("clickedFeed");

        feedUserName.setText(feed.getUsername());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        String formattedDate = sdf.format(new Date(Long.parseLong(feed.getDate())));
        feedDate.setText(formattedDate);
        Glide.with(this).load(feed.getImageUri()).into(feedPicture);
        feedText.setText(feed.getContent());
    }
}