package com.boris.delivery.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.boris.delivery.R;
import com.boris.delivery.dto.MissionDTO;

import java.util.List;

public class MissionAdapter extends RecyclerView.Adapter<MissionAdapter.MissionViewHolder>{
    private List<MissionDTO> items;

    public MissionAdapter(List<MissionDTO> items){
        this.items = items;
    }
    @NonNull
    @Override
    public MissionAdapter.MissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mission, parent, false);
        return new MissionAdapter.MissionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MissionAdapter.MissionViewHolder holder, int position) {
        MissionDTO mission = items.get(position);
        holder.tvDate.setText(mission.getDate().toString());
        holder.tvRef.setText(mission.getId());
        holder.tvDeliverer.setText(mission.getDelivererEmail());
        holder.switchIsRealised.setChecked(mission.isRealised());
        holder.switchIsAccepted.setChecked(mission.isAccepted());

        // Rendre les commutateurs non cliquables
        holder.switchIsRealised.setClickable(false);
        holder.switchIsAccepted.setClickable(false);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class MissionViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvDeliverer;
        TextView tvRef;
        Switch switchIsRealised;
        Switch switchIsAccepted;
        public MissionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvRef = itemView.findViewById(R.id.tvRef);
            tvDeliverer = itemView.findViewById(R.id.tvDeliverer);
            switchIsRealised = itemView.findViewById(R.id.switchIsRealised);
            switchIsAccepted = itemView.findViewById(R.id.switchIsAccepted);
        }
    }
}
