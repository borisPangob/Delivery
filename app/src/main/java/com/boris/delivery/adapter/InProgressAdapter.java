package com.boris.delivery.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.boris.delivery.deliveryInterface.OnItemClickListener;
import com.boris.delivery.R;
import com.boris.delivery.dto.MissionDTO;

import java.util.List;

public class InProgressAdapter extends RecyclerView.Adapter<InProgressAdapter.InProgressViewHolder>{
    private List<MissionDTO> items;
    private OnItemClickListener itemClickListener;

    public InProgressAdapter(List<MissionDTO> items, OnItemClickListener itemClickListener){
        this.items = items;
        this.itemClickListener = itemClickListener;
    }
    @NonNull
    @Override
    public InProgressAdapter.InProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_in_progress, parent, false);
        return new InProgressAdapter.InProgressViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull InProgressAdapter.InProgressViewHolder holder, int position) {
        MissionDTO mission = items.get(position);
        holder.tvDate.setText(mission.getDate().toString());
        holder.tvRef.setText(mission.getId());
        int number = mission.getListOfAdrresses().size()-1;
        if(number<0){number = 0;}
        holder.tvNumber.setText(String.valueOf(number));

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {return items.size();}

    public static class InProgressViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvRef;
        TextView tvNumber;
        public InProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvRef = itemView.findViewById(R.id.tvRef);
            tvNumber = itemView.findViewById(R.id.tvNumber);
        }
    }
}
