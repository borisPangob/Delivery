package com.boris.delivery.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.boris.delivery.deliveryInterface.OnButtonClickListener;
import com.boris.delivery.R;
import com.boris.delivery.dto.BookDTO;
import com.boris.delivery.dto.OrderDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrdersViewHolder>{

    private List<OrderDTO> itemOrders;
    private List<BookDTO> itemBooks;
    private OnButtonClickListener buttonClickListener;
    private Map<String, BookDTO> bookOfOrder;

    public OrdersAdapter(List<OrderDTO> itemOrders, List<BookDTO> itemBooks, OnButtonClickListener deleteClickListener) {
        this.itemOrders = itemOrders;
        this.itemBooks = itemBooks;
        this.buttonClickListener = deleteClickListener;
        this.bookOfOrder = new HashMap<>();
    }

    @NonNull
    @Override
    public OrdersAdapter.OrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_panier, parent, false);
        return new OrdersAdapter.OrdersViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull OrdersAdapter.OrdersViewHolder holder, int position) {
        OrderDTO order = itemOrders.get(position);
        BookDTO book =this.getAssociedBook(order.getIdBook());

        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthor());
        holder.tvPrice.setText(String.valueOf(book.getPrice())+" €");
        holder.tvQuantity.setText(String.valueOf(order.getQuantity()));
        holder.rtBook.setRating((float) book.getRating());
        holder.imageBook.setImageResource((int) book.getImage());

        holder.btnincrease.setOnClickListener(v -> {
            buttonClickListener.onIncreaseClick(position);
        });
        holder.btndecrease.setOnClickListener(v -> {
            buttonClickListener.onDecreaseClick(position);
        });
        holder.delete.setOnClickListener(v -> {
            buttonClickListener.onDeleteClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return itemOrders.size();
    }

    public BookDTO getAssociedBook(String idBook){
        BookDTO book =new BookDTO();
        for (BookDTO item : itemBooks) {
            if (item.getId().equals(idBook)) {
                book = item;
            }
        }
        return book;
    }

    public static class OrdersViewHolder extends RecyclerView.ViewHolder{
        TextView tvTitle;
        TextView tvAuthor;
        TextView tvPrice;
        TextView tvQuantity;
        RatingBar rtBook;
        ImageView imageBook;
        Button btnincrease;
        Button btndecrease;
        Button delete;
        public OrdersViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            rtBook = itemView.findViewById(R.id.rtBook);
            imageBook = itemView.findViewById(R.id.imageBook);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnincrease = itemView.findViewById(R.id.increaseQte);
            btndecrease = itemView.findViewById(R.id.decreaseQte);
            delete = itemView.findViewById(R.id.delete);
        }
    }
}
