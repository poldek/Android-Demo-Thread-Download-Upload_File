package com.pgmsoft.photocarshop.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.pgmsoft.photocarshop.R;
import com.pgmsoft.photocarshop.Utils;
import com.pgmsoft.photocarshop.restapi.ImageModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImageRecyclerViewList extends RecyclerView.Adapter<ImageRecyclerViewList.MessageHolder> {

    private List<ImageModel> modelCardViewData;
    private Context context;
    private OnItemClickListener onItemClickListener;


    public ImageRecyclerViewList(Context context) {
        this.modelCardViewData = new ArrayList<>();
        this.context = context;
    }


    public void listImage(List<ImageModel> modelCardViewData) {
        if (modelCardViewData.size() > 0) {
            this.modelCardViewData = modelCardViewData;
            notifyDataSetChanged();
        }
    }



    @Override
    public int getItemCount() {
        return  modelCardViewData.size();
    }


    public String getFullPathImage(int position) {
        return modelCardViewData.get(position).getFull();
    }

    public void clearListCache() {
        modelCardViewData.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.row_image,parent,false);
        return new MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {

        ImageModel imageModel = this.modelCardViewData.get(position);
        holder.imageFullPath.setText(imageModel.getFull());
        holder.imageFullName.setText(imageModel.getFile());
        holder.imageSize.setText(Utils.bytesIntoHumanReadable(imageModel.getSize()));
        holder.imageTime.setText(Utils.timeConvert(imageModel.getTime()));
        Glide
                .with(context)
                .load(Utils.apkStorage+"/" + imageModel.getFile())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_baseline_broken_image_24)
                .optionalCenterCrop()
                .into(holder.imageView);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.e("Photo " , ":" + Utils.bytesIntoHumanReadable(imageModel.getSize()));
            }
        });
    }

    class MessageHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private final TextView imageFullPath;
        private final TextView imageFullName;
        private final TextView imageSize;
        private final TextView imageTime;


        MessageHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView2);
            imageFullPath = itemView.findViewById(R.id.imgFullPath);
            imageFullName = itemView.findViewById(R.id.textViewFullName);
            imageSize = itemView.findViewById(R.id.textViewImageSize);
            imageTime = itemView.findViewById(R.id.textViewImageTime);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(modelCardViewData.get(position));
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ImageModel imageModel);
    }

    public void setOnItemClickListener (OnItemClickListener onItemClickListener) {

        this.onItemClickListener = onItemClickListener;
    }

}

