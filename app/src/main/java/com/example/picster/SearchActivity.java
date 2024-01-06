package com.example.picster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SearchActivity extends AppCompatActivity {
    BottomNavigationView navigationView;
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
    }
}