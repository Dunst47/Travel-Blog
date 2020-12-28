package com.dunsthaze.mrblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dunsthaze.mrblog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

public class OnePost extends AppCompatActivity {
    ImageView singeImage;
    TextView singeTitle,singeDesc;
    String post_key = null;
    FirebaseFirestore firestore;
    Button deleteBtn;
    FirebaseAuth auth;
    ScrollView linearLayout;
    String currentUerId ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_post);

        firestore = FirebaseFirestore.getInstance();
        singeImage = findViewById(R.id.singleImageview);
        singeTitle = findViewById(R.id.singleTitle);
        singeDesc = findViewById(R.id.singleDesc);
        deleteBtn = findViewById(R.id.deleteBtn);
        auth = FirebaseAuth.getInstance();
        deleteBtn.setVisibility(View.INVISIBLE);
        post_key = getIntent().getExtras().getString("post_id");

        linearLayout = findViewById(R.id.lato);

        currentUerId = auth.getCurrentUser().getUid();
        AnimationDrawable animationDrawable =(AnimationDrawable) linearLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firestore.collection("Post").document(post_key).delete();
                sendToMain();
            }
        });

        firestore.collection("Post").document(post_key).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    String postTitle = task.getResult().getString("title");
                    String blog = task.getResult().getString("blog");
                    String imageUrl = task.getResult().getString("imageurl");
                    String postUId = task.getResult().getString("user_id");


                    singeTitle.setText(postTitle);
                    singeDesc.setText(blog);
                    Glide.with(OnePost.this).load(imageUrl).into(singeImage);
                    if (auth.getCurrentUser().getUid().equals(postUId)){
                        deleteBtn.setVisibility(View.VISIBLE);
                    }
                }
            }
        });






    }

    private void sendToMain () {
        Intent intent = new Intent(OnePost.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}