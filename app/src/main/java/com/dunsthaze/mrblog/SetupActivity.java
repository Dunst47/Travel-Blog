package com.dunsthaze.mrblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dunsthaze.mrblog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText profUserName;
    private FirebaseAuth firebaseAuth;
    private StorageReference mStorageRef;
    private ProgressBar progressBar;
    private Uri profileImageUri = null;
    private final static int GALLERY_REQ = 1;
    CircleImageView circleImageView;
    private int REQUEST_C = 123;
    FirebaseFirestore firestore;
    String username,user_id;


    Button save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Toolbar toolbar = findViewById(R.id.toolbarSetup);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Details");


        circleImageView = findViewById(R.id.circle);
        profUserName = findViewById(R.id.names);
        save = findViewById( R.id.saveChanges);
        progressBar = findViewById(R.id.setprogbar);


        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        user_id = firebaseAuth.getCurrentUser().getUid();

        firestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        profUserName.setText(name);
                        Glide.with(SetupActivity.this).load(image).into(circleImageView);
                    }
                }else {
                    Toast.makeText(SetupActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = profUserName.getText().toString();
                progressBar.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(username) && profileImageUri != null) {

                    final String user_id = firebaseAuth.getCurrentUser().getUid();
                    final StorageReference ppFolderRef = FirebaseStorage.getInstance().getReference().child("profile_images").child(user_id);
                    final StorageReference ppRef = FirebaseStorage.getInstance().getReference().child("profile_images").child(user_id + "jpg");

                    ppFolderRef.putFile(profileImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            if (taskSnapshot.getMetadata() != null) {
                                if (taskSnapshot.getMetadata().getReference() != null) {
                                    Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            final String profim = uri.toString();
                                            Map<String, String> userMap = new HashMap<>();
                                            userMap.put("name",username);
                                            userMap.put("image",profim);
                                            firestore.collection("Users").document(user_id)
                                                    .set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Toast.makeText(SetupActivity.this,"Details updated",Toast.LENGTH_SHORT).show();
                                                        sendToMain();
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                    }else {
                                                        Toast.makeText(SetupActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            }

                        }

                    });
                }
            }
        });


                    circleImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_C);
                                } else {
                                    bringImagePicker();
                                }
                            } else {
                                bringImagePicker();
                            }
                        }
                    });


                }

                private void sendToMain () {
                    Intent intent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                private void bringImagePicker () {
                    CropImage.activity()
                            .setAspectRatio(1, 1)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(SetupActivity.this);
                }

                @Override
                public void onActivityResult ( int requestCode, int resultCode, Intent data){
                    super.onActivityResult(requestCode, resultCode, data);
                    if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                        CropImage.ActivityResult result = CropImage.getActivityResult(data);
                        if (resultCode == RESULT_OK) {
                            profileImageUri = result.getUri();
                            circleImageView.setImageURI(profileImageUri);
                        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                            Exception error = result.getError();
                        }
                    }
                }
            }
