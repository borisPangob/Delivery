package com.boris.delivery.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.boris.delivery.R;
import com.boris.delivery.dto.DeliveryDTO;

import java.util.List;

public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.DeliveryViewHolder>{
    private List<DeliveryDTO> items;

    public DeliveryAdapter(List<DeliveryDTO> items){
        this.items = items;
    }
    @NonNull
    @Override
    public DeliveryAdapter.DeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_delivery, parent, false);
        return new DeliveryAdapter.DeliveryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryAdapter.DeliveryViewHolder holder, int position) {
        DeliveryDTO delivery = items.get(position);
        holder.tvEmail.setText(delivery.getEmail());
        holder.tvAdresse.setText(delivery.getAddress());
        holder.tvDate.setText(delivery.getTimestamp().toString());
        holder.tvRef.setText(delivery.getRef());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class DeliveryViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail;
        TextView tvAdresse;
        TextView tvDate;
        TextView tvRef;
        public DeliveryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvAdresse = itemView.findViewById(R.id.tvAdresse);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvRef = itemView.findViewById(R.id.tvRef);
        }
    }
}
