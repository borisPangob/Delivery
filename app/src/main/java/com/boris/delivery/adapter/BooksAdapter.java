package com.boris.delivery.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.boris.delivery.deliveryInterface.OnItemClickListener;
import com.boris.delivery.R;
import com.boris.delivery.dto.BookDTO;

import java.util.List;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BooksViewHolder> {

    private List<BookDTO> items;
    private OnItemClickListener itemClickListener;

    public BooksAdapter(List<BookDTO> items, OnItemClickListener itemClickListener) {
        this.items = items;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public BooksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BooksViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BooksViewHolder holder, int position) {
        BookDTO book = items.get(position);
        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthor());
        holder.tvSummary.setText(book.getSummary());
        holder.rtBook.setRating((float) book.getRating());
        holder.imageBook.setImageResource((int) book.getImage());

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class BooksViewHolder extends RecyclerView.ViewHolder{
        TextView tvTitle;
        TextView tvAuthor;
        TextView tvSummary;
        RatingBar rtBook;
        ImageView  imageBook;
        public BooksViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvSummary = itemView.findViewById(R.id.tvSummary);
            rtBook = itemView.findViewById(R.id.rtBook);
            imageBook = itemView.findViewById(R.id.imageBook);
        }
    }

}
