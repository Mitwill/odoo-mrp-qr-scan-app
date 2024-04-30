package com.mitwill.mrp.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.mitwill.mrp.R;
import com.mitwill.mrp.interfaces.RecyclerViewItemClickListener;

import java.io.File;
import java.util.List;

public class QuentityImageListAdapter extends RecyclerView.Adapter<QuentityImageListAdapter.MyViewHolder> {

    private Context context;
    private List<String> imageList;
    private RecyclerViewItemClickListener recyclerViewItemClickListener;

    public QuentityImageListAdapter(Context context, List<String> imageList, RecyclerViewItemClickListener recyclerViewItemClickListener) {
        this.context = context;
        this.imageList = imageList;
        this.recyclerViewItemClickListener = recyclerViewItemClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View listItem = layoutInflater.inflate(R.layout.recyclerview_image_list, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context)
                .load(Uri.fromFile(new File(imageList.get(position))))
                .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(10)))
                .into(holder.ivImagePreview);

        holder.ivRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerViewItemClickListener.onItemClick(holder.getLayoutPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivImagePreview,ivRemoveImage;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.ivImagePreview = itemView.findViewById(R.id.ivImagePreview);
            this.ivRemoveImage = itemView.findViewById(R.id.ivRemoveImage);
        }
    }
}
