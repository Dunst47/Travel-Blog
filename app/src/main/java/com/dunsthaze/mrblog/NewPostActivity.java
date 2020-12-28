package com.dunsthaze.mrblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dunsthaze.mrblog.CoreHelper;
import com.dunsthaze.mrblog.CustomModel;
import com.dunsthaze.mrblog.ImagesAdapter;
import com.dunsthaze.mrblog.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL;

public class NewPostActivity extends AppCompatActivity {


    EditText blog,bTitle;
    ImageView pickImages;
    CoreHelper coreHelper;
    FirebaseStorage storage;
    FirebaseFirestore firestore;
    CollectionReference reference;
    StorageReference storageReference;
    List<CustomModel> imagesList;
    FloatingActionButton add;
    Button post;
    List<String> savedImagesUri;
    RecyclerView recyclerView;
    int counter;
    ImagesAdapter adapter;
    LinearLayout lnrImages;
    ProgressBar progressBar;
    LinearLayout linearLayout;
    FirebaseAuth auth;
    String currentUserId;
    FusedLocationProviderClient fusedLocationProviderClient;
    String address;
    Double longitude;
    Double latitude;

    private static final int READ_PERMISSION_CODE = 1;
    private static final int PICK_IMAGE_REQUEST_CODE = 2;
    private int REQUEST_C = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        Toolbar toolbar = findViewById(R.id.toolbarNewPost);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        reference = firestore.collection("Post");
        storageReference = FirebaseStorage.getInstance().getReference();
        currentUserId = auth.getCurrentUser().getUid();


        blog = findViewById(R.id.edit_story);
        bTitle = findViewById(R.id.blog_title);
        pickImages = findViewById(R.id.blogPic);
        add = findViewById(R.id.addPic);
        lnrImages = findViewById(R.id.lnrImages);
        progressBar = findViewById(R.id.newPostprogbar);
        post = findViewById(R.id.postBlog);
        linearLayout = findViewById(R.id.linearLayout);

        add.setVisibility(View.INVISIBLE);
        imagesList = new ArrayList<>();
        savedImagesUri = new ArrayList<>();
        coreHelper = new CoreHelper(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        recyclerView = findViewById(R.id.imagesRecyclerView);
        adapter = new ImagesAdapter(this,imagesList);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (adapter.getItemCount() != 0) {
                    pickImages.setVisibility(View.GONE);
                    lnrImages.setVisibility(View.VISIBLE);
                } else {
                    pickImages.setVisibility(View.VISIBLE);
                    lnrImages.setVisibility(View.GONE);
                }
            }
        });
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyPermisions();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyPermisions();
            }
        });



        post.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                        uploadImages(v);

                    }

        });

    }

    private void verifyPermisions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //If permission is granted
                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {

                        if (task.isSuccessful()) {

                            Toast.makeText(NewPostActivity.this,"Location services started",Toast.LENGTH_SHORT).show();
                            Location location = task.getResult();
                            if (location!= null) {

                                try {
                                    Geocoder geocoder = new Geocoder(NewPostActivity.this, Locale.getDefault());
                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                                    longitude = addresses.get(0).getLongitude();
                                    latitude = addresses.get(0).getLatitude();
                                    address = addresses.get(0).getAddressLine(0);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(NewPostActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
                                }


                            }

                        }
                    }
                });
                pickImage();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION}, READ_PERMISSION_CODE);
            }
        } else {
            //no need to check permissions in android versions lower then marshmallow
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {

                    if (task.isSuccessful()) {
                        Toast.makeText(NewPostActivity.this,"Location services started",Toast.LENGTH_SHORT).show();
                        Location location = task.getResult();
                        if (location!= null) {

                            try {
                                Geocoder geocoder = new Geocoder(NewPostActivity.this, Locale.getDefault());
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(NewPostActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
                            }


                        }

                    }
                }
            });
            pickImage();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImage();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);

    }




    private void uploadImages(View view) {
        if (imagesList.size() != 0) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploaded 0/"+imagesList.size());
            progressDialog.setCanceledOnTouchOutside(false); //Remove this line if you want your user to be able to cancel upload
            progressDialog.setCancelable(false);    //Remove this line if you want your user to be able to cancel upload
            progressDialog.show();
            final StorageReference storageReference = storage.getReference();
            for (int i = 0; i < imagesList.size(); i++) {
                final int finalI = i;
                storageReference.child("userData/").child(imagesList.get(i).getImageName()).putFile(imagesList.get(i).getImageURI()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            storageReference.child("userData/").child(imagesList.get(finalI).getImageName()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    counter++;
                                    progressDialog.setMessage("Uploaded "+counter+"/"+imagesList.size());
                                    if (task.isSuccessful()){
                                        savedImagesUri.add(task.getResult().toString());
                                    }else{
                                        storageReference.child("userData/").child(imagesList.get(finalI).getImageName()).delete();
                                        Toast.makeText(NewPostActivity.this, "Couldn't save "+imagesList.get(finalI).getImageName(), Toast.LENGTH_SHORT).show();
                                    }
                                    if (counter == imagesList.size()){
                                        saveImageDataToFirestore(progressDialog);
                                    }
                                }
                            });
                        }else{
                            progressDialog.setMessage("Uploaded "+counter+"/"+imagesList.size());
                            counter++;
                            Toast.makeText(NewPostActivity.this, "Couldn't upload "+imagesList.get(finalI).getImageName(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } else {
            coreHelper.createSnackBar(view, "Please add some images first.", "", null, Snackbar.LENGTH_SHORT);
        }
    }
    private void saveImageDataToFirestore(final ProgressDialog progressDialog) {

        progressDialog.setMessage("Saving uploaded images...");
        Map<String, Object> map = new HashMap<>();



        final Handler handler = new Handler();
        String blogMain = blog.getText().toString();
        String title = bTitle.getText().toString();

        java.util.Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM-dd-yyyy");
        final String saveCDate = currentDate.format(calendar.getTime());

        java.util.Calendar calendar1 = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        final String saveCTime = currentTime.format(calendar1.getTime());


            map.put("blog", blogMain);
            map.put("title", title);
            map.put("latitude",latitude);
            map.put("longitude",longitude);
            map.put("address", address);
            map.put("date", saveCDate);
            map.put("time", saveCTime);
            map.put("user_id", currentUserId);
            for (int i = 0; i < savedImagesUri.size(); i++) {
                map.put("imageurl", savedImagesUri.get(i));

            }
            reference.add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        coreHelper.createAlert("Success", "Blog uploaded and saved successfully!", "OK", "", null, null, null);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sendToMain();
                            }
                        }, 1500);


                    } else {
                        progressDialog.dismiss();
                        coreHelper.createAlert("Error", "Images uploaded but we couldn't save them to database.", "OK", "", null, null, null);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sendToMain();
                            }
                        }, 1500);
                    }
                }
            });
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_IMAGE_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    ClipData clipData = data.getClipData();
                    if (clipData != null) {
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            Uri uri = clipData.getItemAt(i).getUri();
                            imagesList.add(new CustomModel(coreHelper.getFileNameFromUri(uri), uri));
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Uri uri = data.getData();
                        imagesList.add(new CustomModel(coreHelper.getFileNameFromUri(uri), uri));
                        adapter.notifyDataSetChanged();
                    }
                }
        }
    }
    private void sendToMain() {
        Intent mainintent = new Intent(NewPostActivity.this,MainActivity.class);
        startActivity(mainintent);
        finish();
    }
}