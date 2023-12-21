package com.example.picster;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    EditText inputEmail, inputPassword;
    Button loginButton, createButton;
    SignInButton googleLoginButton;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        googleLoginButton = findViewById(R.id.googleLoginButton);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        loginButton = findViewById(R.id.loginButton);
        createButton = findViewById(R.id.createButton);

        // 로그아웃 기능구현 - 나중에 재사용하기!
//        createButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                firebaseAuth.signOut();
//                mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(MainActivity.this, "Logout failed", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//            }
//        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(MainActivity.this);

        // 자동 로그인 기능 - 한 번 유저가 구글 로그인으로 로그인 했던 기록 있으면 로그아웃 전까지 자동 로그인됨 (유저 편의성)
//        if(gsa != null) {
//            Toast.makeText(MainActivity.this, "Log in Success!", Toast.LENGTH_SHORT).show();
//        }

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();

                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    if(email.equals("admin@gmail.com")) { // admin
                                        Toast.makeText(MainActivity.this, "Hello, admin!", Toast.LENGTH_SHORT).show();
//                                        Intent intent = new Intent(MainActivity.this, AdminActivity.class);
//                                        startActivity(intent);
                                    } else { // user
                                        Toast.makeText(MainActivity.this, "Log in Success!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                                        startActivity(intent);
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "Login failed!\nCheck your email & password again.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            successGoogleLogin(user);
                        } else {
                            successGoogleLogin(null);
                        }
                    }
                });
    }

    private void successGoogleLogin(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(MainActivity.this, "Log in Success!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(MainActivity.this, "Log in failed", Toast.LENGTH_SHORT).show();
        }
    }
}