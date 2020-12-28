package com.dunsthaze.mrblog;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dunsthaze.mrblog.Models.BlogPost;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blogPostList;
    Context context;
    FirebaseFirestore firestore;
    FirebaseAuth auth;


    public BlogRecyclerAdapter(List<BlogPost> blogList){
        this.blogPostList = blogList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_item, parent, false);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.setIsRecyclable(false);
        final String postId = blogPostList.get(position).blogPostId;
        final String currentuser_ud = auth.getCurrentUser().getUid();
        String title = blogPostList.get(position).getTitle();
        holder.setTitle(title);
        final String imageUrl = blogPostList.get(position).getImageurl() ;
        holder.setImageUrl(imageUrl);
        final String time = blogPostList.get(position).getTime();
        holder.setTime(time);
        final String date = blogPostList.get(position).getDate();
        holder.setDate(date);

        final String user;

        final String user_id = blogPostList.get(position).getUser_id();
        firestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){


                    String username = task.getResult().getString("name");
                    String userimage = task.getResult().getString("image");
                    holder.setUserData(username,userimage);
                }else {

                }
            }
        });
        String address = blogPostList.get(position).getAddress();
        holder.setAddress(address);

        firestore.collection("Post/"+postId+"/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value!= null && !value.isEmpty()){
                    int count = value.size();
                    holder.updateLikesCount(count);
                    holder.likes.setText(R.string.likes);
                    if (count == 1){
                        holder.likes.setText(R.string.like);
                    }else {
                        holder.likes.setText(R.string.likes);
                    }
                }else {
                    holder.updateLikesCount(0);
                }
            }
        });
        holder.postLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent singleIn = new Intent(context,OnePost.class);
                singleIn.putExtra("post_id",postId);
                singleIn.putExtra("userId",currentuser_ud);
                context.startActivity(singleIn);
            }
        });

        firestore.collection("Post/"+postId+"/Likes").document(currentuser_ud).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value!= null && value.exists()){
                    holder.like.setImageResource(R.drawable.like);
                }else {
                    Log.e("MyTag", "Firebase exception", error);
                    holder.like.setImageResource(R.drawable.dislike);
                }
            }
        });



        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firestore.collection("Post/"+postId+"/Likes").document(currentuser_ud).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.getResult().exists()){



                            holder.like.setImageResource(R.drawable.dislike);
                            Map<String, Object> map = new HashMap<>();
                            map.put("timestamp", FieldValue.serverTimestamp());
                            map.put("image",imageUrl);
                            map.put("postId",postId);
                            firestore.collection("Post/"+postId+"/Likes").document(currentuser_ud).set(map);
                        }else {

                            holder.like.setImageResource(R.drawable.like);
                            firestore.collection("Post/"+postId+"/Likes").document(currentuser_ud).delete();
                        }
                    }
                });
            }
        });


    }

    @Override
    public int getItemCount() {
        return blogPostList.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageButton like ;
        TextView likeCount,likes;
        View mView;
        TextView postTitle,username,date,time,address;
        ImageView profilrpic,imageView;
        LinearLayout postLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            like = itemView.findViewById(R.id.like_button);
            postLayout = itemView.findViewById(R.id.linear_layout_post);
            likes = itemView.findViewById(R.id.likes);
        }
        public void updateLikesCount(int count){
            likeCount = mView.findViewById(R.id.likes_display);
            likeCount.setText(Integer.toString(count));

        }

        public void setTitle(String title){
            postTitle = mView.findViewById(R.id.post_title_txtview);
            postTitle.setText(title);
        }
        public void setUserData(String username1, String image){
            username = mView.findViewById(R.id.post_user);
            profilrpic = mView.findViewById(R.id.userImage);
            username.setText(username1);
            Glide.with(context).load(image).into(profilrpic);

        }public void setTime(String time1){
            time = mView.findViewById(R.id.time);
            time.setText(time1);

        }
        public void setDate(String date1){
            date = mView.findViewById(R.id.dateb);
            date.setText(date1);

        }
        public void setAddress(String address1){
            address = mView.findViewById(R.id.address);
            address.setText(address1);

        }
        public void setImageUrl(String imageUrl1){
            imageView = mView.findViewById(R.id.post_image);
            Glide.with(context).load(imageUrl1).into(imageView);
        }

    }

}
