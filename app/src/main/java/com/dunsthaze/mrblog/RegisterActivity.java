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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    EditText emailreg,passwordreg,conpassreg;
    Button signinreg,signupreg;
    ProgressBar progressBarReg;
    FirebaseDatabase database;
    DatabaseReference serDetails;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        serDetails = database.getReference().child("Users");


        emailreg = findViewById(R.id.emailreg);
        passwordreg = findViewById(R.id.passwordreg);
        conpassreg = findViewById(R.id.conpasswordreg);
        signinreg = findViewById(R.id.signinbtnreg);
        signupreg = findViewById(R.id.signupbtnreg);
        progressBarReg = findViewById(R.id.regprogbar);
        signinreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        signupreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailreg.getText().toString();
                String password = passwordreg.getText().toString();
                String conpassword = conpassreg.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(conpassword)){
                    if (password.equals(conpassword)){
                        progressBarReg.setVisibility(View.VISIBLE);

                        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task){
                                if (task.isSuccessful()){
                                    sendToSetup();

                                }else {
                                    Toast.makeText(RegisterActivity.this, task.getException().getMessage(),Toast.LENGTH_LONG).show();

                                }
                                progressBarReg.setVisibility(View.INVISIBLE);
                            }
                        });

                    }else {
                        Toast.makeText(RegisterActivity.this,"Passwords do not match",Toast.LENGTH_LONG).show();
                    }
                }

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currntUser = auth.getCurrentUser();
        if (currntUser != null){
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent mainintent = new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(mainintent);
        finish();
    }

    private void sendToLogin() {
        Intent mainintent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(mainintent);
        finish();
    }
    private void sendToSetup() {
        Intent mainintent = new Intent(RegisterActivity.this,SetupActivity.class);
        startActivity(mainintent);
        finish();
    }

}