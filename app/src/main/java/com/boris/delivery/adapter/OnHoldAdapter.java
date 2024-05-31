package com.boris.delivery.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.boris.delivery.deliveryInterface.OnButtonClickListener;
import com.boris.delivery.R;
import com.boris.delivery.dto.MissionDTO;

import java.util.List;


public class OnHoldAdapter extends RecyclerView.Adapter<OnHoldAdapter.OnHoldViewHolder>{
    private List<MissionDTO> items;
    private OnButtonClickListener buttonClickListener;

    public OnHoldAdapter(List<MissionDTO> items, OnButtonClickListener buttonClickListener){
        this.items = items;
        this.buttonClickListener = buttonClickListener;
    }
    @NonNull
    @Override
    public OnHoldAdapter.OnHoldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_on_hold, parent, false);
        return new OnHoldAdapter.OnHoldViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OnHoldAdapter.OnHoldViewHolder holder, int position) {
        MissionDTO mission = items.get(position);
        holder.tvDate.setText(mission.getDate().toString());
        holder.tvRef.setText(mission.getId());
        //On crée un arrayAdapter qui utilise un tableau de String et un spinner layout par défaut
        ArrayAdapter<String> AddressAdapter = new ArrayAdapter<>(holder.itemView.getContext(), android.R.layout.simple_spinner_item, mission.getListOfAdrresses());
        //On spécifie le layout qui sera utilisé quand la liste des choix apparait
        AddressAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerAdrresses.setAdapter(AddressAdapter);
        holder.btnAccepter.setOnClickListener(v -> {
            buttonClickListener.onValidateClick(position);
        });
        holder.btnRefuser.setOnClickListener(v -> {
            buttonClickListener.onDeclineClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class OnHoldViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvRef;
        Spinner spinnerAdrresses;
        Button btnAccepter;
        Button btnRefuser;
        public OnHoldViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvRef = itemView.findViewById(R.id.tvRef);
            spinnerAdrresses = itemView.findViewById(R.id.spinnerAdrresses);
            btnAccepter = itemView.findViewById(R.id.btnAccepter);
            btnRefuser = itemView.findViewById(R.id.btnRefuser);
        }
    }
}
