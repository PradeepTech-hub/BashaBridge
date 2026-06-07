package com.example.pbl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> {

    private List<Word> contentList;
    private OnEditClickListener editListener;
    private OnDeleteClickListener deleteListener;

    public interface OnEditClickListener {
        void onEdit(Word word);
    }

    public interface OnDeleteClickListener {
        void onDelete(Word word);
    }

    public ContentAdapter(List<Word> contentList, OnEditClickListener editListener, OnDeleteClickListener deleteListener) {
        this.contentList = contentList;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Word word = contentList.get(position);
        holder.tvEnglish.setText(word.getEnglish());
        holder.tvMeanings.setText(word.getKannada() + " / " + word.getHindi());
        holder.tvMetadata.setText("Std: " + word.getStandard() + " | Category: " + word.getCategory());

        if (word.getImageUrl() != null && !word.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(word.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.ivContentImage);
        } else {
            holder.ivContentImage.setImageResource(word.getImageResId() != 0 ? word.getImageResId() : android.R.drawable.ic_menu_gallery);
        }

        holder.btnEdit.setOnClickListener(v -> editListener.onEdit(word));
        holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(word));
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEnglish, tvMeanings, tvMetadata;
        ImageButton btnEdit, btnDelete;
        ImageView ivContentImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEnglish = itemView.findViewById(R.id.tvEnglish);
            tvMeanings = itemView.findViewById(R.id.tvMeanings);
            tvMetadata = itemView.findViewById(R.id.tvMetadata);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            ivContentImage = itemView.findViewById(R.id.ivContentImage);
        }
    }
}
