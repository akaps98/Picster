package com.example.picster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingActivity extends AppCompatActivity {
    BottomNavigationView navigationView;
    Button changeUsernameButton, changePasswordButton, deleteAccountButton, friendListButton, activityButton, logoutButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        navigationView = findViewById(R.id.bottom_navigation);
        changeUsernameButton = findViewById(R.id.changeUsernameButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);
        friendListButton = findViewById(R.id.friendListButton);
        activityButton = findViewById(R.id.activityButton);
        logoutButton = findViewById(R.id.logoutButton);

        changeUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, ChangeUsernameActivity.class);
                startActivity(intent);
            }
        });

        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, DeleteAccountActivity.class);
                startActivity(intent);
            }
        });

        navigationView.setSelectedItemId(R.id.navigation_setting);

        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int selected = item.getItemId();

                if (selected == R.id.navigation_search) {
                    Intent searchIntent = new Intent(SettingActivity.this, SearchActivity.class);
                    startActivityForResult(searchIntent, 1);
                } else if (selected == R.id.navigation_bookmarks) {
                    Intent bookmarkIntent = new Intent(SettingActivity.this, BookmarkActivity.class);
                    startActivityForResult(bookmarkIntent, 2);
                } else if (selected == R.id.navigation_home) {
                    Intent homeIntent = new Intent(SettingActivity.this, DashboardActivity.class);
                    startActivityForResult(homeIntent, 3);
                } else if (selected == R.id.navigation_user) {
                    Intent myPageIntent = new Intent(SettingActivity.this, MyPageActivity.class);
                    startActivityForResult(myPageIntent, 4);
                } else if (selected == R.id.navigation_setting) {
                    // same activity; stay
                }
                return true;
            }
        });
    }
}