package com.dunsthaze.mrblog;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dunsthaze.mrblog.Models.BlogPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView blogView;
    AfterPostImageAdapter adapter;
    List<CustomModel> imagesList;
    List<BlogPost> blogPostList;
    FirebaseFirestore firebaseFirestore;
    BlogRecyclerAdapter blogRecyclerAdapter;
    FirebaseAuth auth;
    DocumentSnapshot lastsnapshot;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        blogPostList = new ArrayList<>();
        blogView = view.findViewById(R.id.blogRecyclerView);
        blogRecyclerAdapter = new BlogRecyclerAdapter(blogPostList);
        auth = FirebaseAuth.getInstance();

        firebaseFirestore = FirebaseFirestore.getInstance();
        blogView.setHasFixedSize(true);
        blogView.setLayoutManager(new LinearLayoutManager(getActivity()));
        blogView.setAdapter(blogRecyclerAdapter);


        if (auth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();



            firebaseFirestore.collection("Post").orderBy("date",Query.Direction.DESCENDING).orderBy("time",Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                    if (value != null) {

                        for (DocumentSnapshot doc : value) {
                            if (doc.exists()) {
                                String blogPostId = doc.getId();
                                BlogPost blogPost = doc.toObject(BlogPost.class).withId(blogPostId);
                                blogPostList.add(blogPost);
                                blogRecyclerAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            });
        }

        return view;
    }

}