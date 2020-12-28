package com.dunsthaze.mrblog;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dunsthaze.mrblog.Models.BlogPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AccountFragment extends Fragment {

    ArrayList<String> notification;
    private RecyclerView notView;
    public List<BlogPost> blogPostList;
    BlogRecyclerAdapter blogRecyclerAdapter;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth auth;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        auth = FirebaseAuth.getInstance();
        blogPostList = new ArrayList<>();
        notView = view.findViewById(R.id.notRecyclerView);
        blogRecyclerAdapter = new BlogRecyclerAdapter(blogPostList);
        notView.setHasFixedSize(true);
        notView.setLayoutManager(new LinearLayoutManager(getActivity()));
        notView.setAdapter(blogRecyclerAdapter);

        if (auth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection("Post").whereEqualTo("user_id",auth.getCurrentUser().getUid()).orderBy("date", Query.Direction.DESCENDING).orderBy("time", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                    if (value != null) {
                        for (DocumentChange doc : value.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String blogPostId = doc.getDocument().getId();
                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
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