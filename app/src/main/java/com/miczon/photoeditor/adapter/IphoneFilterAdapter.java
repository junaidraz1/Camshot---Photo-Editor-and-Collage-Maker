package com.miczon.photoeditor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.miczon.photoeditor.R;

import java.util.ArrayList;

public class IphoneFilterAdapter extends RecyclerView.Adapter<IphoneFilterAdapter.ImageViewHolder> {

    Context context;
    ArrayList<Integer> filterPreviews;

    public IphoneFilterAdapter(Context context, ArrayList<Integer> filterPreviews) {
        this.context = context;
        this.filterPreviews = filterPreviews;
    }

    @NonNull
    @Override
    public IphoneFilterAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_cam_filter_preview, parent, false);
        return new IphoneFilterAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IphoneFilterAdapter.ImageViewHolder holder, int position) {
        holder.filterPreviewImage.setImageResource(filterPreviews.get(position));

    }

    @Override
    public int getItemCount() {
        return filterPreviews.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView filterPreviewImage;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            filterPreviewImage = itemView.findViewById(R.id.iv_image);
        }
    }
}