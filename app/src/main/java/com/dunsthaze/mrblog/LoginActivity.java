package com.dunsthaze.mrblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dunsthaze.mrblog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText emaillog,passlog;
    Button signinbtn,logsignupbtn;
    FirebaseAuth auth;
    ProgressBar loginprogressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        emaillog = findViewById(R.id.emailsin);
        passlog = findViewById(R.id.passwordsin);
        signinbtn = findViewById(R.id.signinbtn);
        logsignupbtn = findViewById(R.id.signupbtn);
        loginprogressBar = findViewById(R.id.loginprogbar);

        signinbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loginemail = emaillog.getText().toString();
                String loginpassword = passlog.getText().toString();
                if (!TextUtils.isEmpty(loginemail) && !TextUtils.isEmpty(loginpassword)){
                    loginprogressBar.setVisibility(View.VISIBLE);
                    auth.signInWithEmailAndPassword(loginemail,loginpassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                sendtoMain();
                            }else {
                                Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }
                            ;loginprogressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });

        logsignupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null){

            sendtoMain();
        }
    }

    private void sendtoMain() {
        Intent mainintent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(mainintent);
        finish();
    }

}