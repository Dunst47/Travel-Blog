package com.dunsthaze.mrblog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.bumptech.glide.Glide;
import com.dunsthaze.mrblog.R;

/*This adapter is required only if you are going to show the list of selected images*/
public class AfterPostImageAdapter extends RecyclerView.Adapter<AfterPostImageAdapter.ImageViewHolder> {

    Context context;
    List<CustomModel> imagesList;

    public AfterPostImageAdapter(Context context, List<CustomModel> imagesList) {
        this.context = context;
        this.imagesList = imagesList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picked_image_layout, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, final int position) {
        holder.imageName.setText(imagesList.get(position).getImageName());
        Glide.with(context).load(imagesList.get(position).imageURI).into(holder.imageView);
        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, imagesList.get(position).getImageName()+" removed!", Toast.LENGTH_SHORT).show();
                imagesList.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder{
        TextView imageName;
        ImageButton btnRemove;
        ImageView imageView;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageName = itemView.findViewById(R.id.txtImageName);
            btnRemove = itemView.findViewById(R.id.btnRemoveImage);
            imageView = itemView.findViewById(R.id.listImage);
        }
    }
}
